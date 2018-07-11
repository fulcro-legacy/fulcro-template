(ns fulcro-template.ui.user
  (:require [fulcro.client.primitives :as prim :refer [defui]]
   #?(:clj  [fulcro.client.dom-server :as dom] 
      :cljs [fulcro.client.dom :as dom])))

(defui ^:once User
  static prim/IQuery
  (query [this] [:login/error :uid :name :email])
  static prim/Ident
  (ident [this props] [:user/by-id (:uid props)])
  Object
  (render [this]
    (dom/span (get (prim/props this) :name))))

(def ui-user (prim/factory User {:keyfn :uid}))
