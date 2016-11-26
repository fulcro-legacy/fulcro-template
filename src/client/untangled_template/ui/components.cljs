(ns untangled-template.ui.components
  (:require
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]))

(defui PlaceholderImage
  Object
  (render [this]
    (let [{:keys [w h label]} (om/props this)
          label (or label (str w "x" h))]
      (dom/svg #js {:width w :height h}
               (dom/rect #js {:width w :height h :style #js {:fill        "rgb(200,200,200)"
                                                             :strokeWidth 2
                                                             :stroke      "black"}})
               (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label)))))

(def ui-placeholder (om/factory PlaceholderImage))
