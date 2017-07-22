(ns fulcro-template.server
  (:require
    [fulcro.server :as core]
    [com.stuartsierra.component :as component]

    [org.httpkit.server :refer [run-server]]
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
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.cookies :as cookies]

    [ring.util.request :as req]
    [ring.util.response :as response]
    [fulcro.client.util :as util]
    [fulcro.server-render :as ssr]
    [fulcro-template.ui.user :as user]
    [clojure.string :as str]
    [fulcro.i18n :as i18n]
    [fulcro.client.mutations :as m]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SERVER-SIDE RENDERING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  [handler user-db]
  (fn [req]
    (let [uid         (some-> req :session :uid)            ; The UID is stored in server session store if they are logged in
          user        (users/get-user user-db uid)
          logged-in?  (boolean user)
          uri         (:uri req)
          bidi-match  (bidi/match-route routing/app-routes uri) ; where they were trying to go. NOTE: This is shared code with the client!
          valid-page? (boolean bidi-match)
          language    (some-> req :headers (get "accept-language") (str/split #",") first keyword)]

      ; . no valid bidi match. BYPASS. We don't handle it.
      (if valid-page?
        (render-page uri bidi-match user language)
        (handler req)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SERVER
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; To handle the end of the processing chain.
(defn not-found [req]
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "Resource not found."})

; A place to put user's sessions. See Ring wrap-session.
; We could use Ring's in memory session store, but this one is a component that will reset on server restarts
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

; You need at least one of these. Specifies how to handle client requests, and participates in the component system
; of the server. Libraries can provide modules to install on your server to add functionality in a composable fashion.
(defrecord APIModule []
  core/Module
  (system-key [this] :api-module)                           ; this module will be known in the component system as :api-module. Allows you to inject the module.
  (components [this] {})                                    ; Additional components to build. This allows library modules to inject other dependencies that it needs into the system. Typically empty for applications.
  core/APIHandler
  (api-read [this] core/server-read)                        ; using fulcro multimethods so defmutation et al work.
  (api-mutate [this] core/server-mutate))

; A component that creates the full server middleware and stores it under the key :full-server-middleware (used by the web server).
; Your modules (e.g. APIModule above) are composed into one api-handler function by the fulcro-system function, which in
; turn is placed in the component system  under the ; key :fulcro.server/api-handler.
; The :fulcro.server/api-handler component in turn has a key :middleware whose value is the middleware for handling API requests from the client.
; So, you a component (CustomMiddleware) that composes *that* API middleware function
; into the larger whole. In our application our full-server-middleware needs the user
; database and session store (which are injected).
(defrecord CustomMiddleware [full-server-middleware api-handler session-store user-db]
  component/Lifecycle
  (stop [this] (dissoc this :full-server-middleware))
  (start [this]
    (let [wrap-api (:middleware api-handler)]
      ; The chained middleware function needs to be *stored* at :full-server-middleware,
      ; because we're using a Fulcro web server and it expects to find it there.
      (assoc this :full-server-middleware
                  (-> not-found
                    (wrap-resource "public")
                    wrap-api                                ; from fulcro-system modules. Handles /api
                    (wrap-server-side-rendering user-db)
                    (session/wrap-session {:store session-store})
                    core/wrap-transit-params
                    core/wrap-transit-response
                    wrap-content-type
                    wrap-not-modified
                    wrap-params
                    wrap-gzip)))))

(def http-kit-opts
  [:ip :port :thread :worker-name-prefix
   :queue-size :max-body :max-line])

; A component that creates a web server and hooks lifecycle up to it. The server-middleware-component (CustomMiddleware)
; and config are injected.
(defrecord WebServer [config ^CustomMiddleware server-middleware-component server port]
  component/Lifecycle
  (start [this]
    (try
      (let [config         (:value config)                  ; config is a component, must pull out value
            server-opts    (select-keys config http-kit-opts)
            port           (:port server-opts)
            middleware     (:full-server-middleware server-middleware-component)
            started-server (run-server middleware server-opts)]
        (timbre/info (str "Web server (http://localhost:" port ")") "started successfully. Config of http-kit options:" server-opts)
        (assoc this :port port :server started-server))
      (catch Exception e
        (timbre/fatal "Failed to start web server " e)
        (throw e))))
  (stop [this]
    (if-not server this
                   (do (server)
                       (timbre/info "web server stopped.")
                       (assoc this :server nil)))))

; Injection configuration time! The Stuart Sierra component system handles all of the injection. You simple create
; components, place them into the system under a key, and wrap them with (component/using ...) in order to specify what they need.
; When the system is started, the dependencies will be analyzed and started in the correct order (leaves to tree top).
(defn make-system
  "Builds the server component system, which can then be started/stopped. `config-path` is a relative or absolute path
  to a web server configuration EDN file."
  [config-path]
  (core/fulcro-system
    {:components {:config                      (core/new-config config-path) ; you MUST use config if you use our server
                  ; Needed by Ring sessions. Composed in the Ring stack above
                  :session-store               (map->SessionStore {})
                  ; Creation/injection of the middleware stack
                  :server-middleware-component (component/using
                                                 (map->CustomMiddleware {})
                                                 ; the middleware needs the composed module's api handler, session store, and user database
                                                 {:api-handler   :fulcro.server/api-handler ; remap the generated api handler's key to api-handler, so it is easier to use there
                                                  :session-store :session-store
                                                  :user-db       :user-db})
                  ; The storage for user's who have signed up
                  :user-db                     (users/map->InMemoryUserDB {})
                  ; The web server itself, which needs the config and full-stack middleware.
                  :web-server                  (component/using (map->WebServer {})
                                                 [:config :server-middleware-component])}
     ; Modules are composable into the API handler (each can have their own read/mutate) and
     ; are joined together in a chain that is injected into the component system as :fulcro.server/api-handler
     :modules    [(component/using (map->APIModule {})
                    ; the things injected here will be available in the modules' parsing env
                    [:session-store :user-db])]}))

;; SEE: COMMIT 992d88d3005c6933c57c0416f557101507bd4681 in the Git History for an example using the Easy Server
