(ns untangled-template.intro
  (:require [devcards.core :as rc :refer-macros [defcard]]
            [om.next :as om :refer-macros [defui]]
            [untangled-template.ui.components :as comp]
            [om.dom :as dom]))

(defcard SVGPlaceholder
         "# SVG Placeholder"
         (comp/ui-placeholder {:w 200 :h 200}))
