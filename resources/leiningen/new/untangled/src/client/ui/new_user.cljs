(ns {{name}}.ui.new-user
  (:require
    [om.next :as om :refer-macros [defui]]
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    [om.dom :as dom]
    [om-css.core :as css :refer-macros [localize-classnames]]
    [untangled.client.mutations :as mut]))

(defui ^:once NewUser
  static uc/InitialAppState
  (initial-state [this params] {:id :new-user :ui/username "" :ui/password "" :ui/password2 ""})
  static om/IQuery
  (query [this] [:id :ui/username :ui/password :ui/password2])
  static om/Ident
  (ident [this props] [:new-user :page])
  Object
  (render [this]
    (localize-classnames NewUser
      (let [{:keys [ui/username ui/password ui/password2]} (om/props this)]
        (dom/div nil
          (dom/div #js {:className "row"}
            (dom/div #js {:className "col-xs-4"} "")
            (dom/div #js {:class [:form :$col-xs-4]}
              (dom/div #js {:className "form-group"}
                (dom/label #js {:htmlFor "username"} "Email Address")
                (dom/input #js {:className "form-control" :type "email" :name "username" :value username
                                :onChange  #(mut/set-string! this :ui/username :event %)}))
              (dom/div #js {:className "form-group"}
                (dom/label #js {:htmlFor "password"} "Password")
                (dom/input #js {:name     "password" :className "form-control"
                                :type     "password" :value password
                                :onChange #(mut/set-string! this :ui/password :event %)}))
              (dom/div #js {:className "form-group"}
                (dom/label #js {:htmlFor "password2"} "Verify your Password")
                (dom/input #js {:name     "password2" :className "form-control"
                                :type     "password" :value password2
                                :onChange #(mut/set-string! this :ui/password2 :event %)}))
              (dom/button #js {:onClick #(om/transact! this `[(new-user/sign-up {:uid ~(om/tempid) :u ~username :p ~password})])} "Sign Up!"))))))))

(def ui-new-user (om/factory NewUser))

