(ns fulcro-template.server
  (:require
    [fulcro.server :as core]
    [fulcro.easy-server :as easy]
    [com.stuartsierra.component :as component]

    [om.next.server :as om]
    [fulcro-template.api.read :as r]
    [fulcro-template.api.mutations :as mut]

    [taoensso.timbre :as timbre]
    [ring.middleware.session :as session]
    [ring.middleware.session.store :as store]
    [ring.middleware.resource :as resource]
    [ring.util.request :as req]
    [ring.middleware.cookies :as cookies]
    [ring.util.response :as response]
    [fulcro.client.util :as util]))

(defn wrap-html5-routes-as-index [handler]
  (fn [req]
    (let [url       (req/request-url req)
          dev-mode? (boolean (System/getProperty "dev"))
          real-html (if dev-mode? "/index-dev.html" "/index.html")
          ; only serve index in place of things that do not have a suffix, or end in .html
          is-leaf?  (boolean (re-matches #".*/([^/.]*|[^/.]*\.html)$" url))]
      (if is-leaf?
        (-> (resource/resource-request (assoc req :uri real-html) "public")
          (response/content-type "text/html"))
        (handler req)))))

(defrecord HTML5Route [handler]
  component/Lifecycle
  (start [this]
    (let [vanilla-pipeline (easy/get-fallback-hook handler)]
      (easy/set-fallback-hook! handler (comp vanilla-pipeline
                                         (partial wrap-html5-routes-as-index))))
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
                                  (map->HTML5Route {})
                                  [:handler])}))
