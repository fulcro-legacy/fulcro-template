(ns fulcro-template.ui.main
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.alpha.i18n :refer [tr]]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as m]))

(defsc MainPage [this {:keys [:current-user]}]
  {:initial-state {:id :main}
   :query         [:id [:current-user '_]]
   :ident         (fn [] [:main :page])}
  (dom/div #js {} (tr "Main page")))
