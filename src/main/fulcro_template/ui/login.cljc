(ns fulcro-template.ui.login
  (:require [om.next :as om :refer [defui]]
            [fulcro.i18n :refer [tr]]
            [fulcro.client.core :as u]
            [fulcro.client.data-fetch :as df]
            [om.dom :as dom]
            [fulcro-template.api.mutations :as api]
            [fulcro.client.mutations :as m]
            [fulcro-template.ui.html5-routing :as r]
            [fulcro-template.ui.user :as user]
            [fulcro.ui.bootstrap3 :as b]
            [fulcro.events :as evt]))

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
      (b/container-fluid {}
        (b/row {}
          (b/col {:lg-offset 3 :lg 6 :xs-offset 1 :xs 11}
            (dom/div #js {:className "form-horizontal"}
              (b/labeled-input {:id "username" :type "text" :split 3 :onChange #(m/set-string! this :ui/username :event %)} (tr "Email"))
              (b/labeled-input {:id "password" :type "password" :split 3 :onKeyDown (fn [evt] (when (evt/enter-key? evt) (login))) :onChange #(m/set-string! this :ui/password :event %)} (tr "Password"))
              (b/labeled-input {:id              "submit" :split 3
                                :input-generator (fn [props]
                                                   (b/button (merge props {:kind :primary :disabled loading-data :type "submit" :onClick login}) "Login"))} ""))))
        (when server-down
          (b/row {}
            (b/col {:xs-offset 4 :xs 4}
              (b/alert {:kind :warning} (tr "Unable to contact server. Try again later.")))))
        (b/row nil
          (b/col {:xs-offset 4 :xs 4}
            (tr "Don't have a login yet? ")
            (dom/a #js {:onClick #(r/nav-to! this :new-user)} (tr "Sign up!"))))))))
