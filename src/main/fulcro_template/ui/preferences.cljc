(ns fulcro-template.ui.preferences
  (:require [om.next :as om :refer [defui]]
            [fulcro.client.core :as u]
            [fulcro.i18n :refer [tr]]
            [om.dom :as dom]
            [fulcro.client.mutations :as m]))

(defui ^:once PreferencesPage
  static u/InitialAppState
  (initial-state [this params] {:id :preferences})
  static om/IQuery
  (query [this] [:id])
  static om/Ident
  (ident [this props] [:main :page])
  Object
  (render [this]
    (dom/div #js {} (tr "Preferences page"))))
