(ns fulcro-template.ui.main
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.i18n :refer [tr]]
   #?(:clj  [fulcro.client.dom-server :as dom] 
      :cljs [fulcro.client.dom :as dom])))

(defsc MainPage [this {:keys [:current-user]}]
  {:initial-state {:id :main}
   :query         [:id [:current-user '_]]
   :ident         (fn [] [:main :page])}
  (dom/div (tr "Main page")))
