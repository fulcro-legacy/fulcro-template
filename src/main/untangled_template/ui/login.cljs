(ns untangled-template.ui.login
  (:require [om.next :as om :refer [defui]]
            [untangled.i18n :refer [tr]]
            [untangled.client.core :as u]
            [untangled.client.data-fetch :as df]
            [om.dom :as dom]
            [untangled-template.api.mutations :as api]
            [untangled.client.mutations :as m]
            [untangled-template.ui.html5-routing :as r]
            [untangled-template.ui.user :as user]
            [untangled.ui.bootstrap3 :as b]))

(defui ^:once LoginPage
  static u/InitialAppState
  (initial-state [this params] {:id :login :ui/username "" :ui/password "" :ui/server-down false :ui/error nil})
  static om/IQuery
  (query [this] [:id :ui/username :ui/password [:server-down '_] [:ui/loading-data '_]])
  static om/Ident
  (ident [this props] [:login :page])
  Object
  (render [this]
    (let [{:keys [ui/username ui/password server-down ui/loading-data]} (om/props this)
          login (fn []
                  (om/transact! this `[(api/attempt-login {:uid ~(om/tempid) :u ~username :p ~password})
                                       (tx/fallback {:action api/server-down})])
                  (df/load this :logged-in? nil)
                  (df/load this :current-user user/User {:post-mutation `api/login-complete
                                                         :refresh       [:logged-in? :current-user]}))]
      (b/container {}
        (b/row {}
          (b/col {:xs 6 :xs-offset 3}
            (when server-down
              (dom/div nil "Unable to contact server. Try again later."))
            (when loading-data
              (dom/div nil "Working..."))
            (dom/div #js {:className "form-horizontal"}
              (b/labeled-input {:id "username" :type "text" :split 2 :onChange #(m/set-string! this :ui/username :event %)} (tr "Username"))
              (b/labeled-input {:id "password" :type "password" :split 2 :onChange #(m/set-string! this :ui/password :event %)} (tr "Password"))
              (dom/div #js {:className "form-group"}
                (dom/div #js {:className "col-sm-offset-2 col-sm-10"}
                  (b/button {:type "submit" :onClick login} "Login"))))))
        (b/row nil
          (b/col {:xs-offset 4 :xs 4}
            "Don't have a login yet? "
            (dom/a #js {:onClick #(r/nav-to! this :new-user)} "Sign up!")))))))
