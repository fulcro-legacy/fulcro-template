(ns fulcro-template.ui.preferences
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.i18n :refer [tr]]
   #?(:clj  [fulcro.client.dom-server :as dom] 
      :cljs [fulcro.client.dom :as dom])))

(defsc PreferencesPage [this props]
  {:initial-state {:id :preferences}
   :query         [:id]
   :ident         (fn [] [:main :page])}
  (dom/div (tr "Preferences page")))
