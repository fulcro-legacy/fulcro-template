(ns leiningen.new.untangled
  (:require
    [leiningen.new.tmpl :as tmpl]
    [leiningen.new.templates :as template]
    [leiningen.core.main :as main]
    [clojure.set :as set]
    [stencil.parser :as p]
    [stencil.core :as s]
    [clojure.string :refer [replace-first]]
    [clojure.java.io :as io]))

(defn make-dflt-lambdas [all opts]
  (->> all
       (mapv (fn [opt]
               {(keyword (str "when-"     (name opt))) #(when     (get opts opt) %)
                (keyword (str "when-not-" (name opt))) #(when-not (get opts opt) %)}))
       (apply merge)))

(defn render-files [opts vars]
  (let [data (merge (make-dflt-lambdas tmpl/all opts)
                    vars)
        render #(try ((template/renderer "untangled") % data)
                     (catch Exception e (println "Failed at" %) (throw e)))
        tmpl (fn [files-map]
               (mapv (fn [[from to]]
                       (vector to (render from)))
                     files-map))]
    (->> tmpl/files
         (mapv (fn [[opt-name opt-files]]
                 (if (some-> opt-files meta :always)
                   (tmpl opt-files)
                   (if-not (get opts opt-name) []
                     (tmpl opt-files)))))
         (apply concat)
         (apply template/->files data))))

(defn parse-args [args]
  (letfn [(parse [args]
            (reduce #(->> %2 read-string (conj %1)) #{} args))
          (validate [all args]
            (let [diff (set/difference args all)]
              (assert (empty? diff) (str "invalid args: " diff)))
            args)
          (expand [arg->opts args]
            (->> args
                 (mapcat #(if-let [xs (arg->opts %)] xs [%]))
                 set
                 (reduce #(assoc %1 %2 true) {})))]
    (->> args
      (parse)
      (validate (set/union tmpl/all (set (keys tmpl/opts))))
      (expand tmpl/opts))))

(defn untangled
  [project-name & args]
  (let [opts (parse-args args)]
    (main/info "Generating an untangled project.")
    (main/info (str "With opts: " opts))
    (render-files opts (tmpl/make-data project-name opts))
    (main/info "Done! see README for info")))
