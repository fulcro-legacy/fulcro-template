(ns fulcro-template.ui.components
  (:require [fulcro.client.primitives :as prim :refer [defui]]
   #?(:clj  [fulcro.client.dom-server :as dom] 
      :cljs [fulcro.client.dom :as dom]) ))

(defui ^:once PlaceholderImage
  Object
  (render [this]
    (let [{:keys [w h label]} (prim/props this)
          label (or label (str w "x" h))]
      (dom/svg {:width w :height h}
        (dom/rect {:width w :height h :style {:fill   "rgb(200,200,200)"
                                                      :strokeWidth 2
                                                      :stroke      "black"}})
        (dom/text {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label)))))

(def ui-placeholder (prim/factory PlaceholderImage))
