(ns fulcro-template.ui.user
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as om :refer [defui]]))

(defui ^:once User
  static om/IQuery
  (query [this] [:login/error :uid :name :email])
  static om/Ident
  (ident [this props] [:user/by-id (:uid props)])
  Object
  (render [this]
    (dom/span nil (get (om/props this) :name))))

(def ui-user (om/factory User {:keyfn :uid}))
