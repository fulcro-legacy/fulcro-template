(ns {{name}}.ui.main
  (:require
    [om.next :as om :refer-macros [defui]]
    [untangled.client.core :as uc]
    [om.dom :as dom]
    [om-css.core :as css :refer-macros [localize-classnames]]
    [untangled.client.mutations :as mut]))

(defui ^:once MainPage
  static uc/InitialAppState
  (initial-state [this params] {:id :main})
  static om/IQuery
  (query [this] [:id [:current-user '_]])
  static css/CSS
  (css [this] [[(css/local-kw MainPage :x)]])
  static om/Ident
  (ident [this props] [:main :page])
  Object
  (render [this]
    (localize-classnames MainPage
      (let [{:keys [current-user]} (om/props this)]
        (dom/div #js {:class :form} "MAIN PAGE")))))

(def ui-main (om/factory MainPage))
