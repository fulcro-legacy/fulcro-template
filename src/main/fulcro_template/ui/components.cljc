(ns fulcro-template.ui.components
  (:require
    [fulcro.client.primitives :as om :refer [defui]]
    #?(:cljs [fulcro.client.alpha.dom :as dom] :clj
    [fulcro.client.alpha.dom-server :as dom])))

(defui ^:once PlaceholderImage
  Object
  (render [this]
    (let [{:keys [w h label]} (om/props this)
          label (or label (str w "x" h))]
      (dom/svg {:width w :height h}
        (dom/rect {:width w :height h :style #js {:fill        "rgb(200,200,200)"
                                                  :strokeWidth 2
                                                  :stroke      "black"}})
        (dom/text {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label)))))

(def ui-placeholder (om/factory PlaceholderImage))
