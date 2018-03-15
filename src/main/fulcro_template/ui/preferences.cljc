(ns fulcro-template.ui.preferences
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.i18n :refer [tr]]
    #?(:cljs [fulcro.client.alpha.dom :as dom] :clj
            [fulcro.client.alpha.dom-server :as dom])
            [fulcro.client.mutations :as m]))

(defsc PreferencesPage [this props]
  {:initial-state {:id :preferences}
   :query         [:id]
   :ident         (fn [] [:main :page])}
  (dom/div (tr "Preferences page")))
