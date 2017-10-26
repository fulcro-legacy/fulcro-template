(ns fulcro-template.intro
  (:require [devcards.core :as rc :refer-macros [defcard]]
            [fulcro.client.primitives :as om :refer-macros [defui]]
            [fulcro-template.ui.components :as comp]
            [fulcro.client.dom :as dom]))

(defcard SVGPlaceholder
  "# SVG Placeholder"
  (comp/ui-placeholder {:w 200 :h 200}))
