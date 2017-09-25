(ns fulcro-template.ui.root
  (:require
    [fulcro.client.mutations :as mut]
    [fulcro.client.core :as fc]
    [fulcro.client.util :as util]
    [fulcro.client.routing :refer [defrouter]]
    [fulcro.client.mutations :as m]
    [fulcro.client.logging :as log]
    [om.dom :as dom]
    [fulcro-template.ui.html5-routing :as r]
    [fulcro-template.ui.login :as l]
    [fulcro-template.ui.user :as user]
    [fulcro-template.ui.main :as main]
    [fulcro-template.ui.preferences :as prefs]
    [fulcro-template.ui.new-user :as nu]
    [fulcro-template.api.mutations :as api]
    [om.next :as om :refer [defui]]
    [fulcro.server-render :as ssr]
    [fulcro.i18n :refer [tr]]
    [fulcro.ui.bootstrap3 :as b]))

(defrouter Pages :page-router
  (ident [this props] [(:id props) :page])
  :login l/LoginPage
  :new-user nu/NewUser
  :preferences prefs/PreferencesPage
  :main main/MainPage)

(def ui-pages (om/factory Pages))

(defn ui-login-stats [loading? user logout-fn]
  (dom/p #js {:className "navbar-text navbar-right"}
    (when loading? (b/badge {} "..."))
    (user/ui-user user)
    (dom/br nil)
    (dom/a #js {:onClick logout-fn} (tr "Log out"))))

(defn ui-login-button [loading? login-fn]
  (dom/p #js {:className "navbar-right"}
    (when loading?
      (dom/span #js {:className "navbar-text badge"} "..."))
    (b/button {:className "navbar-btn" :onClick login-fn} (tr "Sign in"))))

(defn ui-navbar [this]
  (let [login      #(r/nav-to! this :login)
        logout     #(om/transact! this `[(api/logout {}) (r/set-route! {:handler :login}) :current-user])
        {:keys [ui/loading-data current-user]} (om/props this)
        logged-in? (contains? current-user :name)]
    (dom/div #js {:className "navbar navbar-default"}
      (dom/div #js {:className "container-fluid"}
        (dom/div #js {:className "navbar-header"}
          (dom/span #js {:className "navbar-brand"}
            (dom/span nil "Template Brand")
            (dom/br nil)
            (dom/a #js {:onClick #(om/transact! this `[(m/change-locale {:lang :en})]) :href "#"} "en") " | "
            (dom/a #js {:onClick #(om/transact! this `[(m/change-locale {:lang :es})]) :href "#"} "es")))
        (dom/div #js {:className "collapse navbar-collapse"}
          (when logged-in?
            (dom/ul #js {:className "nav navbar-nav"}
              ;; More nav links here
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :main)} (tr "Main")))
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :preferences)} (tr "Preferences")))))
          (if logged-in?
            (ui-login-stats loading-data current-user logout)
            (ui-login-button loading-data login)))))))

;; Add other modals here.
(defui ^:once Modals
  static om/IQuery
  (query [this] [{:welcome-modal (om/get-query b/Modal)}])
  static fc/InitialAppState
  (initial-state [this params] {:welcome-modal (fc/get-initial-state b/Modal {:id :welcome :backdrop true})})
  Object
  (render [this]
    (let [{:keys [welcome-modal]} (om/props this)]
      (b/ui-modal welcome-modal
        (b/ui-modal-title nil
          (dom/b nil (tr "Welcome!")))
        (b/ui-modal-body nil
          (dom/p #js {:className b/text-info} (tr "Glad you could join us!")))
        (b/ui-modal-footer nil
          (b/button {:onClick #(om/transact! this `[(b/hide-modal {:id :welcome})])} (tr "Thanks!")))))))

(defui ^:once Root
  static fc/InitialAppState
  (initial-state [c p] (merge
                         {; Is there a user logged in?
                          :logged-in?   false
                          ; Is the UI ready for initial render? This avoids flicker while we figure out if the user is already logged in
                          :ui/ready?    false
                          ; What are the details of the logged in user
                          :current-user nil
                          :root/modals  (fc/get-initial-state Modals {})
                          :pages        (fc/get-initial-state Pages nil)}
                         r/app-routing-tree))
  static om/IQuery
  (query [this] [:ui/react-key :ui/ready? :logged-in?
                 {:current-user (om/get-query user/User)}
                 {:root/modals (om/get-query Modals)}
                 fulcro.client.routing/routing-tree-key     ; TODO: Check if this is needed...seemed to affect initial state from ssr
                 :ui/loading-data {:pages (om/get-query Pages)}])
  Object
  (render [this]
    (let [{:keys [ui/ready? ui/loading-data ui/react-key pages welcome-modal current-user logged-in?] :or {react-key "ROOT"}} (om/props this)]
      (dom/div #js {:key react-key}
        (ui-navbar this)
        (when ready?
          (ui-pages pages))))))
