(ns untangled-template.ui.main
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as u]
            [om.dom :as dom]
            [untangled.client.mutations :as m]))

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
      (dom/div #js {} "MAIN PAGE"))))
