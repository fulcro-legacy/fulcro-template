(ns {{name}}.system
  (:require
    [untangled.server.core :as core]
    {{#when-datomic}}[untangled.datomic.core :as udc]{{/when-datomic}}
    [om.next.server :as om]
    [{{name}}.api.read :as r]
    [{{name}}.api.mutations :as mut]

    [taoensso.timbre :as timbre]))

(defn logging-mutate [env k params]
  (timbre/info "Entering mutation:" k)
  (mut/apimutate env k params))

(defn make-system [config-path]
  (core/make-untangled-server
    :config-path config-path
    :parser (om/parser {:read r/api-read :mutate logging-mutate})
    :parser-injections #{:config {{#when-datomic}}:{{name}}-database{{/when-datomic}}}
    :components { {{#when-datomic}}
                 :{{name}}-database (udc/build-database :{{name}})
                 :logger {}
                 {{/when-datomic}} }))
