(ns untangled-template.system
  (:require
    [untangled.server.core :as core]
    [com.stuartsierra.component :as component]

    [om.next.server :as om]
    [untangled-template.api.read :as r]
    [untangled-template.api.mutations :as mut]
    [untangled.server.impl.components.handler :as h]

    [taoensso.timbre :as timbre]
    [ring.middleware.cookies :as cookies]
    [ring.util.response :as response]))

(defn logging-mutate [env k params]
  (timbre/info "Entering mutation:" k)
  (mut/apimutate env k params))

; TEMPLATE: :examples
(defrecord AdditionalPipeline [handler]
  component/Lifecycle
  (start [this]
    ; This is the basic pattern for composing into the existing pre-hook handler (which starts out as identity)
    ; If you're sure this is the only component hooking in, you could simply set it instead.
    (let [vanilla-pipeline (h/get-pre-hook handler)]
      (h/set-pre-hook! handler (comp vanilla-pipeline
                                     (partial cookies/wrap-cookies))))
    this)
  (stop [this] this))

(defn make-system [config-path]
  (core/make-untangled-server
    :config-path config-path
    :parser (om/parser {:read r/api-read :mutate logging-mutate})
    :parser-injections #{:config}
    ; TEMPLATE: :examples
    ; An example for providing extra route handlers (e.g. REST-based endpoints for non-Om clients)
    :extra-routes {:routes   [["/hello/" [#"\d+" :id]] :sample1] ; See docs in Bidi for making route defs
                   ; Use the route target keyword to define the function that handles that route
                   :handlers {:sample1 (fn [env {:keys [route-params]}]
                                         (timbre/info "Got a request on sample1 route with " :ENV env :ROUTE-PARAMS route-params)
                                         (-> (str "Hello: " (:id route-params))
                                             response/response
                                             (response/content-type "text/plain")))}}
    ; TEMPLATE: :examples
    :components {:pipeline (component/using                 ; add some additional wrappers to the pipeline
                             (map->AdditionalPipeline {})
                             [:handler])}))
