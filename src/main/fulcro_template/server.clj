(ns fulcro-template.server
  (:require
    [fulcro.server :as core]
    [fulcro.easy-server :as easy]
    [com.stuartsierra.component :as component]

    [om.next.server :as om]
    [fulcro-template.api.read :as r]
    [fulcro-template.api.mutations :as mut]
    [om.next :refer [tree->db db->tree factory get-query]]
    [fulcro-template.api.user-db :as users]
    [om.dom :as dom]

    [fulcro-template.ui.root :as root]
    [fulcro-template.ui.html5-routing :as routing]
    [fulcro.client.core :as fc]

    [bidi.bidi :as bidi]
    [taoensso.timbre :as timbre]
    [ring.middleware.session :as session]
    [ring.middleware.session.store :as store]
    [ring.middleware.resource :as resource]
    [ring.util.request :as req]
    [ring.middleware.cookies :as cookies]
    [ring.util.response :as response]
    [fulcro.client.util :as util]
    [fulcro.server-render :as ssr]
    [fulcro-template.ui.user :as user]
    [clojure.string :as str]
    [fulcro.i18n :as i18n]
    [fulcro.client.mutations :as m]))

(defn top-html
  "Render the HTML for the SPA. There is only ever one kind of HTML to send, but the initial state and initial app view may vary.
  This function takes a normalized client database and a root UI class and generates that page."
  [normalized-client-state root-component-class]
  ; props are a "view" of the db. We use that to generate the view, but the initial state needs to be the entire db
  (let [props                (db->tree (get-query root-component-class) normalized-client-state normalized-client-state)
        root-factory         (factory root-component-class)
        app-html             (dom/render-to-str (root-factory props))
        initial-state-script (ssr/initial-state->script-tag normalized-client-state)]
    (str "<!DOCTYPE) html>\n"
      "<html lang='en'>\n"
      "<head>\n"
      "<meta charset='UTF-8'>\n"
      "<meta name='viewport' content='width=device-width, initial-scale=1'>\n"
      "<link href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css' rel='stylesheet'>\n"
      initial-state-script
      "<title>Home Page (Dev Mode)</title>\n"
      "</head>\n"
      "<body>\n"
      "<div class='container-fluid' id='app'>"
      app-html
      "</div>\n"
      "<script src='js/fulcro_template.js' type='text/javascript'></script>\n"
      "</body>\n"
      "</html>\n")))

(defn build-app-state
  "Builds an up-to-date app state based on the URL where the db will contain everything needed. Returns a normalized
  client app db."
  [user uri bidi-match language]
  (let [base-state       (ssr/build-initial-state (root/initial-app-state-tree) root/Root) ; start with a normalized db that includes all union branches. Uses client UI!
        logged-in?       (boolean user)
        ; NOTE: All of these state functions are CLIENT code that we're leveraging on the server!
        set-route        (fn [s]
                           (if logged-in?
                             (fulcro.client.routing/update-routing-links s bidi-match)
                             (fulcro.client.routing/update-routing-links s {:handler :login})))
        set-user         (fn [s] (-> s
                                   (fc/merge-component user/User user)
                                   (assoc :logged-in? true :current-user (util/get-ident user/User user))))
        ; Augment the database with the detected route details and user.
        ; Also mark the app as ready, so the UI will render on the server, AND the client can detect that it was a server render.
        normalized-state (cond-> base-state
                           logged-in? set-user
                           (not logged-in?) (assoc :loaded-uri uri)
                           language (m/change-locale-impl language)
                           set-route (set-route)
                           :always (assoc :ui/ready? true))]
    normalized-state))

(defn render-page
  "Server-side render the entry page."
  [uri match user language]
  (let [normalized-app-state (build-app-state user uri match language)]
    (-> (top-html normalized-app-state root/Root)
      response/response
      (response/content-type "text/html"))))

(defn wrap-server-side-rendering
  "Ring middleware to handle all sends of the SPA page(s) via server-side rendering. If you want to see the client
  without SSR, just remove this component from the ring stack and supply an index.html in resources/public."
  [handler]
  (fn [req]
    (let [uid         (some-> req :session :uid)            ; The UID is stored in server session store if they are logged in
          user        (users/get-user uid)
          logged-in?  (boolean user)
          uri         (:uri req)
          bidi-match  (bidi/match-route routing/app-routes uri) ; where they were trying to go. NOTE: This is shared code with the client!
          valid-page? (boolean bidi-match)
          language    (some-> req :headers (get "accept-language") (str/split #",") first keyword)]

      (timbre/info req)
      (timbre/info "Desired language " language)

      ; . no valid bidi match. BYPASS. We don't handle it.
      (if valid-page?
        (render-page uri bidi-match user language)
        (handler req)))))

(defrecord ServerSideRenderer [handler]
  component/Lifecycle
  (start [this]
    (let [vanilla-pipeline (easy/get-pre-hook handler)]
      (easy/set-pre-hook! handler (comp vanilla-pipeline
                                    (partial wrap-server-side-rendering))))
    this)
  (stop [this] this))

(defrecord RingSessions [handler session-store]
  component/Lifecycle
  (start [this]
    ; This is the basic pattern for composing into the existing pre-hook handler (which starts out as identity)
    ; If you're sure this is the only component hooking in, you could simply set it instead.
    (let [vanilla-pipeline (easy/get-pre-hook handler)]
      (easy/set-pre-hook! handler (comp vanilla-pipeline
                                    (fn [h] (session/wrap-session h {:store session-store})))))
    this)
  (stop [this] this))

(defrecord SessionStore [memory-store]
  store/SessionStore
  (read-session [_ key]
    (get @memory-store key))
  (write-session [_ key data]
    (let [key (or key (util/unique-key))]
      (swap! memory-store assoc key data)
      key))
  (delete-session [_ key]
    (swap! memory-store dissoc key)
    nil)
  component/Lifecycle
  (start [this] (assoc this :memory-store (atom {})))
  (stop [this] this))

(defn make-system [config-path]
  (easy/make-fulcro-server
    :config-path config-path
    :parser (core/fulcro-parser)
    :parser-injections #{:config :session-store}
    #_(comment
        ; An example for providing extra route handlers (e.g. REST-based endpoints for non-Om clients)
        :extra-routes {:routes   [["/hello/" [#"\d+" :id]] :sample1] ; See docs in Bidi for making route defs
                       ; Use the route target keyword to define the function that handles that route
                       :handlers {:sample1 (fn [env {:keys [route-params]}]
                                             (timbre/info "Got a request on sample1 route with " :ENV env :ROUTE-PARAMS route-params)
                                             (-> (str "Hello: " (:id route-params))
                                               response/response
                                               (response/content-type "text/plain")))}})
    ; TEMPLATE: Sessions and handling of HTML5 routes (not real files)
    :components {:sessions      (component/using
                                  (map->RingSessions {})
                                  [:handler :session-store])
                 :session-store (map->SessionStore {})
                 :html5-routes  (component/using
                                  (map->ServerSideRenderer {})
                                  ; Technically, the server-side renderer does not use the session by direct code reference, but it needs it to go "first"
                                  [:handler :session-store])}))
