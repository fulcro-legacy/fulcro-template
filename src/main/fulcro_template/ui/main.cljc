(ns fulcro-template.ui.main
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.i18n :refer [tr]]
    #?(:cljs [fulcro.client.alpha.dom :as dom] :clj
            [fulcro.client.alpha.dom-server :as dom])
            [fulcro.client.mutations :as m]))

(defsc MainPage [this {:keys [current-user]}]
  {:initial-state {:id :main}
   :query         [:id [:current-user '_]]
   :ident         (fn [] [:main :page])}
  (dom/div (tr "Main page")))
