(ns fulcro-template.ui.preferences
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client :as u]
            [fulcro.alpha.i18n :refer [tr]]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as m]))

(defsc PreferencesPage [this props]
  {:initial-state {:id :preferences}
   :query         [:id]
   :ident         (fn [] [:main :page])}
  (dom/div #js {} (tr "Preferences page")))
