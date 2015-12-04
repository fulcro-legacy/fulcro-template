(ns {{name}}.intro
  (:require
    [devcards.core :as rc :refer-macros [defcard]]
    [om.dom :as dom]
    [{{name}}.ui.components :as comp]))

(defcard intro-card
  "#Intro to Devcards!"
  (dom/div nil "Hello from devcards & om!"))


(defcard SVGPlaceholder
  "# SVG Placeholder"
  (comp/ui-placeholder {:w 200 :h 200}))
