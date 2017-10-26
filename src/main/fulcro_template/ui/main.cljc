(ns fulcro-template.ui.main
  (:require [fulcro.client.primitives :as om :refer [defui]]
            [fulcro.client.core :as u]
            [fulcro.i18n :refer [tr]]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as m]))

(defui ^:once MainPage
  static u/InitialAppState
  (initial-state [this params] {:id :main})
  static om/IQuery
  (query [this] [:id [:current-user '_]])
  static om/Ident
  (ident [this props] [:main :page])
  Object
  (render [this]
    (let [{:keys [current-user]} (om/props this)]
      (dom/div #js {} (tr "Main page")))))
