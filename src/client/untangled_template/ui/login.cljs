(ns untangled-template.ui.login
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as u]
            [untangled.client.data-fetch :as f]
            [om.dom :as dom]
            [om-css.core :as css :refer-macros [localize-classnames]]
            [untangled.client.mutations :as m]))

(defmethod m/mutate 'login/attempt-login [{:keys [state]} k {:keys [uid]}]
  {:remote true
   :action (fn [] (swap! state assoc
                         :current-user {:id uid :name "???"}
                         :server-down false))})

(defmethod m/mutate 'login/server-down [{:keys [state]} k p]
  {:action (fn [] (swap! state assoc :server-down true))})

(defmethod m/mutate 'login/login-complete [{:keys [state]} k p]
  {:action (fn []
             (let [{:keys [logged-in? current-user]} @state]
               (if logged-in?
                 (swap! state assoc :current-page [:main :page])
                 (swap! state assoc :current-page [:login :page]))))})

(defmethod m/mutate 'login/logout [{:keys [state]} k p]
  {:remote true
   :action (fn []
             (swap! state assoc
                    :current-user {}
                    :logged-in? false
                    :current-page [:login :page]))})

(defui ^:once LoginPage
  static u/InitialAppState
  (initial-state [this params] {:id :login :ui/username "" :ui/password "" :ui/server-down false :ui/error nil})
  static om/IQuery
  (query [this] [:id :ui/username :ui/password [:server-down '_] [:ui/loading-data '_]])
  static css/CSS
  (css [this] [[(css/local-kw LoginPage :form)]])
  static om/Ident
  (ident [this props] [:login :page])
  Object
  (render [this]
    (localize-classnames LoginPage
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
                      (dom/button #js {:onClick #(om/transact! this `[(login/attempt-login {:uid ~(om/tempid) :u ~username :p ~password})
                                                                      (tx/fallback {:action login/server-down})
                                                                      (untangled/load {:query [:logged-in? :current-user] :post-mutation login/login-complete})
                                                                      :ui/react-key
                                                                      :logged-in?
                                                                      :current-user
                                                                      ])} "Login")))
          (dom/div #js {:className "row"}
            (dom/div #js {:className "col-xs-4"} "")
            (dom/div #js {:className "col-xs-4"}
              "Don't have a login yet? "
              (dom/a #js {:onClick #(om/transact! this '[(nav/new-user) :ui/react-key])} "Sign up!"))))))))

(def ui-login (om/factory LoginPage))
