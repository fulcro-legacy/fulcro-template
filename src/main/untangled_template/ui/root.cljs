(ns untangled-template.ui.root
  (:require
    [untangled.client.mutations :as mut]
    [om.dom :as dom]
    [untangled-template.ui.login :as l]
    [untangled-template.ui.main :as main]
    [untangled-template.ui.preferences :as prefs]
    [untangled-template.ui.new-user :as nu]
    [om.next :as om :refer-macros [defui]]
    [untangled.client.core :as u]
    [untangled.client.routing :as r :refer [defrouter]]
    [untangled.client.mutations :as m]
    [untangled.ui.bootstrap3 :as b]
    [untangled-template.api.mutations :as api]))

(defui ^:once Loading
  static u/InitialAppState
  (initial-state [this params] {:id :loading})
  static om/IQuery
  (query [this] [:id])
  static om/Ident
  (ident [this props] [:loading :page])
  Object
  (render [this]
    (dom/div nil "Loading...")))

(defrouter Pages :page-router
  (ident [this props] [(:id props) :page])
  :loading Loading
  :new-user nu/NewUser
  :login l/LoginPage
  :preferences prefs/PreferencesPage
  :main main/MainPage)

(def ui-pages (om/factory Pages))

(defn ui-login-stats [loading? user logout-fn]
  (dom/p #js {:className "navbar-text navbar-right"}
    (when loading? (b/badge {} "..."))
    (:name user)
    (dom/br nil)
    (dom/a #js {:onClick logout-fn} " Log out")))

(defn ui-login-button [loading? login-fn]
  (dom/p #js {:className "navbar-right"}
    (when loading?
      (dom/span #js {:className "navbar-text badge"} "..."))
    (b/button {:className "navbar-btn" :onClick login-fn} "Sign in")))

(defn ui-navbar [this]
  (let [login  #(om/transact! this `[(r/route-to {:handler :login})])
        logout #(om/transact! this `[(api/logout {}) :logged-in? :current-user])
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
              (dom/li nil (dom/a #js {:className "active" :onClick #(om/transact! this `[(r/route-to {:handler :main}) :pages])} "Main"))
              (dom/li nil (dom/a #js {:className "active" :onClick #(om/transact! this `[(r/route-to {:handler :preferences}) :pages])} "Preferences"))))
          (if logged-in?
            (ui-login-stats loading-data current-user logout)
            (ui-login-button loading-data login)))))))

(defui ^:once Root
  static om/IQuery
  (query [this] [:ui/react-key :logged-in? :current-user :ui/loading-data {:pages (om/get-query Pages)}])
  static u/InitialAppState
  (initial-state [this params]
    (merge
      {:logged-in? false :current-user {} :pages (u/get-initial-state Pages nil)}
      (r/routing-tree
        (r/make-route :login [(r/router-instruction :page-router [:login :page])])
        (r/make-route :new-user [(r/router-instruction :page-router [:new-user :page])])
        (r/make-route :preferences [(r/router-instruction :page-router [:preferences :page])])
        (r/make-route :main [(r/router-instruction :page-router [:main :page])])
        (r/make-route :loading [(r/router-instruction :page-router [:loading :page])]))))
  Object
  (render [this]
    (let [{:keys [ui/loading-data ui/react-key pages current-user logged-in?] :or {ui/react-key "ROOT"}} (om/props this)]
      (dom/div #js {:key react-key}
        (ui-navbar this)
        (ui-pages pages)))))
