(ns fulcro-template.ui.root
  (:require
    [fulcro.client.routing :refer [defrouter]]
    [fulcro.client.mutations :as m]
    [fulcro.client.dom :as dom]
    [fulcro-template.ui.html5-routing :as r]
    [fulcro-template.ui.login :as l]
    [fulcro-template.ui.user :as user]
    [fulcro-template.ui.main :as main]
    [fulcro-template.ui.preferences :as prefs]
    [fulcro-template.ui.new-user :as nu]
    [fulcro-template.api.mutations :as api]
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.alpha.i18n :as i18n :refer [tr]]
    [fulcro.ui.bootstrap3 :as b]))

(defrouter Pages :page-router
  (ident [this props] [(:id props) :page])
  :login l/LoginPage
  :new-user nu/NewUser
  :preferences prefs/PreferencesPage
  :main main/MainPage)

(def ui-pages (prim/factory Pages))

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
        logout     #(prim/transact! this `[(api/logout {}) (r/set-route! {:handler :login}) :current-user])
        {:keys [ui/loading-data current-user]} (prim/props this)
        logged-in? (contains? current-user :name)]
    (dom/div #js {:className "navbar navbar-default"}
      (dom/div #js {:className "container-fluid"}
        (dom/div #js {:className "navbar-header"}
          (dom/span #js {:className "navbar-brand"}
            (dom/span nil "Template Brand")
            (dom/br nil)
            (dom/a #js {:onClick #(prim/transact! this `[(i18n/change-locale {:locale :en})]) :href "#"} "en") " | "
            (dom/a #js {:onClick #(prim/transact! this `[(i18n/change-locale {:locale :es})]) :href "#"} "es")))
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
(defsc Modals [this {:keys [welcome-modal]}]
  {:query         [{:welcome-modal (prim/get-query b/Modal)}]
   :initial-state (fn [params] {:welcome-modal (prim/get-initial-state b/Modal {:id :welcome :backdrop true})})}
  (b/ui-modal welcome-modal
    (b/ui-modal-title nil
      (dom/b nil (tr "Welcome!")))
    (b/ui-modal-body nil
      (dom/p #js {:className b/text-info} (tr "Glad you could join us!")))
    (b/ui-modal-footer nil
      (b/button {:onClick #(prim/transact! this `[(b/hide-modal {:id :welcome})])} (tr "Thanks!")))))

(defsc Root [this {:keys [ui/ready? pages ::i18n/current-locale]}]
  {:initial-state (fn [p] (merge
                            {; Is there a user logged in?
                             :logged-in?           false
                             ; Is the UI ready for initial render? This avoids flicker while we figure out if the user is already logged in
                             :ui/ready?            false
                             ; What are the details of the logged in user
                             :current-user         nil
                             :root/modals          (prim/get-initial-state Modals {})
                             ::i18n/current-locale (prim/get-initial-state i18n/Locale {:locale :en :translations {}})
                             :pages                (prim/get-initial-state Pages nil)}
                            r/app-routing-tree))
   :query         [:ui/ready? :logged-in?
                   {::i18n/current-locale (prim/get-query i18n/Locale)}
                   {:current-user (prim/get-query user/User)}
                   {:root/modals (prim/get-query Modals)}
                   fulcro.client.routing/routing-tree-key   ; TODO: Check if this is needed...seemed to affect initial state from ssr
                   :ui/loading-data {:pages (prim/get-query Pages)}]}
  (dom/div nil
    (ui-navbar this)
    (when ready?
      (ui-pages pages))))
