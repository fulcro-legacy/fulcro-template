(ns user
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.stacktrace :refer [print-stack-trace]]

    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [figwheel-sidecar.system :as fig]
    [untangled-template.system :as sys]))

;;FIGWHEEL
(def figwheel (atom nil))

; Usable from a REPL to start one-or-more figwheel builds
(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [figwheel-config (fig/fetch-config)
         props (System/getProperties)
         all-builds (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [figwheel-config (fig/fetch-config)
         default-build-ids (-> figwheel-config :data :build-ids)
         build-ids (if (empty? build-ids) default-build-ids build-ids)
         preferred-config (assoc-in figwheel-config [:data :build-ids] build-ids)]
     (reset! figwheel (component/system-map
                        :css-watcher (fig/css-watcher {:watch-paths ["resources/public/css"]})
                        :figwheel-system (fig/figwheel-system preferred-config)))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (fig/cljs-repl (:figwheel-system @figwheel)))))

;; ==================== SERVER ====================

(set-refresh-dirs "dev/server" "src/server" "specs/server")

(defn started? [sys]
  (-> sys :config :value))

(defonce system (atom nil))
(def cfg-paths {:dev "config/dev.edn"})

(defn refresh [& args]
  {:pre [(not @system)]}
  (apply tools-ns/refresh args))

(defn init [path]
  {:pre [(not (started? @system))
         (get cfg-paths path)]}
  (when-let [new-system (sys/make-system (get cfg-paths path))]
    (reset! system new-system)))

(defn start []
  {:pre [@system (not (started? @system))]}
  (swap! system component/start))

(defn stop []
  (when (started? @system)
    (swap! system component/stop))
  (reset! system nil))

(defn go
  ([] (go :dev))
  ([path] {:pre [(not @system) (not (started? @system))]}
   (init path)
   (start)))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn engage [path & build-ids]
  (stop) (go path) (start-figwheel build-ids))
