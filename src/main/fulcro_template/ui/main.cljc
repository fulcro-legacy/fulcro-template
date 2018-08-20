(ns fulcro-template.ui.main
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.i18n :refer [tr]]
            #?(:cljs [fulcro.client.dom :as dom]
               :clj  [fulcro.client.dom-server :as dom])
            [fulcro-css.css-injection :as injection]
            [fulcro.client.mutations :as m]))

(defsc MainPage [this {:keys [:current-user]} _ {:keys [a]}]
  {:initial-state {:id :main}
   :query         [:id [:current-user '_]]
   :ident         (fn [] [:main :page])}
  (dom/div (tr "Main page")))
