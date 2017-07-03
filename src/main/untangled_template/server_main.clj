(ns untangled-template.server-main
  (:require
    [com.stuartsierra.component :as component]
    [untangled.server :as c]
    [taoensso.timbre :as timbre]
    [untangled-template.system :as sys])
  (:gen-class))

(def config-path "/usr/local/etc/untangled_template.edn")
(def production-config-component (c/new-config config-path))

(defn -main [& args]
  (let [system (sys/make-system config-path)]
    (component/start system)))
