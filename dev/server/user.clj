(ns user
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.stacktrace :refer [print-stack-trace]]

    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.system :as fig]
    [untangled-spec.suite :as suite]
    [untangled-spec.selectors :as sel]
    untangled-template.server))

;;FIGWHEEL
(def figwheel (atom nil))

; Usable from a REPL to start one-or-more figwheel builds
(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [figwheel-config (fig/fetch-config)
         props           (System/getProperties)
         all-builds      (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [figwheel-config   (fig/fetch-config)
         default-build-ids (-> figwheel-config :data :build-ids)
         build-ids         (if (empty? build-ids) default-build-ids build-ids)
         preferred-config  (assoc-in figwheel-config [:data :build-ids] build-ids)]
     (reset! figwheel (component/system-map
                        :css-watcher (fig/css-watcher {:watch-paths ["resources/public/css"]})
                        :figwheel-system (fig/figwheel-system preferred-config)))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (fig/cljs-repl (:figwheel-system @figwheel)))))

;; ==================== SERVER ====================

(set-refresh-dirs "dev/server" "src/main" "src/test" "src/cards")

(defn started? [sys]
  (-> sys :config :value))

(defonce system (atom nil))
(def cfg-paths {:dev "config/dev.edn"})

(defn- refresh [& args]
  {:pre [(not @system)]}
  (apply tools-ns/refresh args))

(defn- init [path]
  {:pre [(not (started? @system))
         (get cfg-paths path)]}
  (when-let [new-system (untangled-template.server/make-system (get cfg-paths path))]
    (reset! system new-system)))

(defn- start []
  {:pre [@system (not (started? @system))]}
  (swap! system component/start))

(defn stop
  "Stop the server."
  []
  (when (started? @system)
    (swap! system component/stop))
  (reset! system nil))

(defn go
  "Initialize the server and start it."
  ([] (go :dev))
  ([path] {:pre [(not @system) (not (started? @system))]}
   (init path)
   (start)))

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (refresh :after 'user/go))

(defn engage
  "Ensure the server is stopped, start the server, then start a figwheel REPL. Useful for vi/emacs."
  [path & build-ids]
  (stop) (go path) (start-figwheel build-ids))

; Run (start-server-tests) in a REPL to start a runner that can render results in a browser
(suite/def-test-suite start-server-tests
  {:config       {:port 8888}
   :test-paths   ["src/test"]
   :source-paths ["dev/server" "src/main"]}
  {:available #{:focused :unit :integration}
   :default   #{::sel/none :focused :unit}})
