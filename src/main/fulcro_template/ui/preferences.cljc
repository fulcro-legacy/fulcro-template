(ns fulcro-template.ui.preferences
  (:require [fulcro.client.primitives :as om :refer [defui]]
            [fulcro.client.core :as u]
            [fulcro.i18n :refer [tr]]
            [fulcro.client.dom :as dom]
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
