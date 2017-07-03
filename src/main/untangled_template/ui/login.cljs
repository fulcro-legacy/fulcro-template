(ns untangled-template.ui.login
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as u]
            [untangled.client.data-fetch :as df]
            [om.dom :as dom]
            [untangled-template.api.mutations :as api]
            [untangled.client.mutations :as m]))

(defui ^:once LoginPage
  static u/InitialAppState
  (initial-state [this params] {:id :login :ui/username "" :ui/password "" :ui/server-down false :ui/error nil})
  static om/IQuery
  (query [this] [:id :ui/username :ui/password [:server-down '_] [:ui/loading-data '_]])
  static om/Ident
  (ident [this props] [:login :page])
  Object
  (render [this]
    (let [{:keys [ui/username ui/password server-down ui/loading-data]} (om/props this)]
      (dom/div nil
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-xs-4"} "")
          (dom/div #js {:class [:form :$col-xs-4]}
            (when server-down
              (dom/div nil "Unable to contact server. Try again later."))
            (when loading-data
              (dom/div nil "Working..."))
            (dom/div #js {:className "form-group"}
              (dom/label #js {:htmlFor "username"} "Username")
              (dom/input #js {:className "form-control" :name "username" :value username
                              :onChange  #(m/set-string! this :ui/username :event %)}))
            (dom/div #js {:className "form-group"}
              (dom/label #js {:htmlFor "password"} "Password")
              (dom/input #js {:name     "password" :className "form-control" :type "password" :value password
                              :onChange #(m/set-string! this :ui/password :event %)}))
            (dom/button #js {:onClick #(om/transact! this `[(api/attempt-login {:uid ~(om/tempid) :u ~username :p ~password})
                                                            (tx/fallback {:action api/server-down})
                                                            (untangled/load {:query         [:logged-in? :current-user]
                                                                             :post-mutation api/login-complete})
                                                            :ui/react-key
                                                            :logged-in?
                                                            :current-user])} "Login")))
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-xs-4"} "")
          (dom/div #js {:className "col-xs-4"}
            "Don't have a login yet? "
            (dom/a #js {:onClick #(om/transact! this `[(api/nav-to {:target :new-user}) :ui/react-key])} "Sign up!")))))))

(def ui-login (om/factory LoginPage))
