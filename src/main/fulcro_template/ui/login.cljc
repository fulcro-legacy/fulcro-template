(ns fulcro-template.ui.login
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.i18n :refer [tr]]
            [fulcro.client :as u]
            [fulcro.client.data-fetch :as df]
            #?(:cljs [fulcro.client.dom :as dom]
               :clj  [fulcro.client.dom-server :as dom])
            [fulcro-template.api.mutations :as api]
            [fulcro.client.mutations :as m]
            [fulcro-template.ui.html5-routing :as r]
            [fulcro-template.ui.user :as user]
            [fulcro.ui.bootstrap3 :as b]
            [fulcro.events :as evt]))

(defsc LoginPage [this {:keys [ui/username ui/password server-down ui/loading-data current-user]}]
  {:initial-state (fn [params] {:id :login :ui/username "" :ui/password "" :ui/server-down false :ui/error nil})
   :query         [:id :ui/username :ui/password [:current-user '_] [:server-down '_] [:ui/loading-data '_]]
   :ident         (fn [] [:login :page])}
  (let [bad-credentials? (contains? current-user :login/error)
        login            (fn []
                           (df/load this :current-user user/User {:post-mutation `api/login-complete
                                                                  :params        {:username username :password password}
                                                                  :refresh       [:logged-in? :current-user]}))]
    (b/container-fluid {}
      (b/row {}
        (b/col {:lg-offset 3 :lg 6 :xs-offset 1 :xs 11}
          (dom/div #js {:className "form-horizontal"}
            (b/labeled-input {:id       "username" :value username :type "text" :split 3
                              :onChange #(m/set-string! this :ui/username :event %)} (tr "Email"))
            (b/labeled-input {:id        "password" :value password :type "password" :split 3
                              :onKeyDown (fn [evt] (when (evt/enter-key? evt) (login))) :onChange #(m/set-string! this :ui/password :event %)} (tr "Password"))
            (b/labeled-input {:id              "submit" :split 3
                              :input-generator (fn [props]
                                                 (b/button (merge props {:kind :primary :disabled loading-data :type "submit" :onClick login}) "Login"))} ""))))
      (cond
        server-down (b/row {}
                      (b/col {:xs-offset 4 :xs 4}
                        (b/alert {:kind :warning} (tr "Unable to contact server. Try again later."))))
        bad-credentials? (b/row {}
                           (b/col {:xs-offset 4 :xs 4}
                             (b/alert {:kind :warning} (tr "Bad username or password.")))))
      (b/row nil
        (b/col {:xs-offset 4 :xs 4}
          (tr "Don't have a login yet? ")
          (dom/a #js {:onClick (fn [evt]
                                 (prim/transact! this `[(api/clear-new-user {})])
                                 (r/nav-to! this :new-user))} (tr "Sign up!")))))))
