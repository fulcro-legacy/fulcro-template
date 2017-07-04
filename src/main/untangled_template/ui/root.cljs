(ns untangled-template.ui.root
  (:require
    [untangled.client.mutations :as mut]
    [om.dom :as dom]
    [untangled-template.ui.html5-routing :as r]
    [untangled-template.ui.login :as l]
    [untangled-template.ui.user :as user]
    [untangled-template.ui.main :as main]
    [untangled-template.ui.preferences :as prefs]
    [untangled-template.ui.new-user :as nu]
    [om.next :as om :refer-macros [defui]]
    [untangled.client.core :as u]
    [untangled.client.routing :refer [defrouter]]
    [untangled.client.mutations :as m]
    [untangled.ui.bootstrap3 :as b]
    [untangled-template.api.mutations :as api]))

(defrouter Pages :page-router
  (ident [this props] [(:id props) :page])
  :new-user nu/NewUser
  :login l/LoginPage
  :preferences prefs/PreferencesPage
  :main main/MainPage)

(def ui-pages (om/factory Pages))

(defn ui-login-stats [loading? user logout-fn]
  (dom/p #js {:className "navbar-text navbar-right"}
    (when loading? (b/badge {} "..."))
    (user/ui-user user)
    (dom/br nil)
    (dom/a #js {:onClick logout-fn} " Log out")))

(defn ui-login-button [loading? login-fn]
  (dom/p #js {:className "navbar-right"}
    (when loading?
      (dom/span #js {:className "navbar-text badge"} "..."))
    (b/button {:className "navbar-btn" :onClick login-fn} "Sign in")))

(defn ui-navbar [this]
  (let [login  #(r/nav-to! this :login)
        logout #(om/transact! this `[(api/logout {}) (r/set-route! {:handler :login}) :logged-in? :current-user])
        {:keys [ui/loading-data current-user logged-in?]} (om/props this)]
    (dom/div #js {:className "navbar navbar-default"}
      (dom/div #js {:className "container-fluid"}
        (dom/div #js {:className "navbar-header"}
          (dom/span #js {:className "navbar-brand"}
            (dom/span nil "Template Brand")))
        (dom/div #js {:className "collapse navbar-collapse"}
          (when logged-in?
            (dom/ul #js {:className "nav navbar-nav"}
              ;; More nav links here
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :main)} "Main"))
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :preferences)} "Preferences"))))
          (if logged-in?
            (ui-login-stats loading-data current-user logout)
            (ui-login-button loading-data login)))))))

(defui ^:once Root
  static om/IQuery
  (query [this] [:ui/react-key :ui/ready? :logged-in? {:current-user (om/get-query user/User)} :ui/loading-data {:pages (om/get-query Pages)}])
  static u/InitialAppState
  (initial-state [this params]
    (merge
      {; Is there a user logged in?
       :logged-in?   false
       ; Is the UI ready for initial render? This avoids flicker while we figure out if the user is already logged in
       :ui/ready?    false
       ; What are the details of the logged in user
       :current-user nil
       :pages        (u/get-initial-state Pages nil)}
      r/app-routing-tree))
  Object
  (render [this]
    (let [{:keys [ui/ready? ui/loading-data ui/react-key pages current-user logged-in?] :or {ui/react-key "ROOT"}} (om/props this)]
      (dom/div #js {:key react-key}
        (ui-navbar this)
        (when ready?
          (ui-pages pages))))))
