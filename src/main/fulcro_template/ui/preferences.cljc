(ns fulcro-template.ui.preferences
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.i18n :refer [tr]]
            #?(:cljs [fulcro.client.dom :as dom]
               :clj  [fulcro.client.dom-server :as dom])
            [fulcro.client.mutations :as m]))

(defsc PreferencesPage [this props]
  {:initial-state {:id :preferences}
   :query         [:id]
   :ident         (fn [] [:main :page])}
  (dom/div #js {} (tr "Preferences page")))
