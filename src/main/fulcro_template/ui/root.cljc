(ns fulcro-template.ui.root
  (:require
    [fulcro.client.mutations :as mut]
    [om.dom :as dom]
    [fulcro-template.ui.html5-routing :as r]
    [fulcro-template.ui.login :as l]
    [fulcro-template.ui.user :as user]
    [fulcro-template.ui.main :as main]
    [fulcro-template.ui.preferences :as prefs]
    [fulcro-template.ui.new-user :as nu]
    [om.next :as om :refer [defui]]
    [fulcro.client.core :as u]
<<<<<<< HEAD:src/main/fulcro_template/ui/root.cljc
    [fulcro.client.util :as util]
    [fulcro.server-render :as ssr]
=======
    [fulcro.i18n :refer [tr]]
>>>>>>> went through and add i18n to all strings. added i18n readme:src/main/fulcro_template/ui/root.cljs
    [fulcro.client.routing :refer [defrouter]]
    [fulcro.client.mutations :as m]
    [fulcro.ui.bootstrap3 :as b]
    [fulcro-template.api.mutations :as api]
    [fulcro.client.core :as uc]
    [fulcro.client.logging :as log]))

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
  (let [login  #(r/nav-to! this :login)
        logout #(om/transact! this `[(api/logout {}) (r/set-route! {:handler :login}) :logged-in? :current-user])
        {:keys [ui/loading-data current-user logged-in?]} (om/props this)]
    (dom/div #js {:className "navbar navbar-default"}
      (dom/div #js {:className "container-fluid"}
        (dom/div #js {:className "navbar-header"}
          (dom/span #js {:className "navbar-brand"}
            (dom/span nil "Template Brand")
            (dom/br nil)
            (dom/a #js {:onClick #(om/transact! this `[(m/change-locale {:lang :en})]) :href "#"} "en") " | "
            (dom/a #js {:onClick #(om/transact! this `[(m/change-locale {:lang :es})]) :href "#"} "es")
            ))
        (dom/div #js {:className "collapse navbar-collapse"}
          (when (true? logged-in?)
            (dom/ul #js {:className "nav navbar-nav"}
              ;; More nav links here
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :main)} (tr "Main")))
              (dom/li nil (dom/a #js {:className "active" :onClick #(r/nav-to! this :preferences)} (tr "Preferences")))))
          (if (true? logged-in?)
            (ui-login-stats loading-data current-user logout)
            (ui-login-button loading-data login)))))))

;; Add other modals here.
(defui ^:once Modals
  static om/IQuery
  (query [this] [{:welcome-modal (om/get-query b/Modal)}])
  static u/InitialAppState
  (initial-state [this params] {:welcome-modal (uc/get-initial-state b/Modal {:id :welcome :backdrop true})})
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

; server-side rendering...we want the server to be able to hand in a completely normalized db for the client to use
(defn initial-app-state-tree []
  (let [default-state (merge
                        {; Is there a user logged in?
                         :logged-in?   false
                         ; Is the UI ready for initial render? This avoids flicker while we figure out if the user is already logged in
                         :ui/ready?    false
                         ; What are the details of the logged in user
                         :current-user nil
                         :root/modals  (uc/get-initial-state Modals {})
                         :pages        (u/get-initial-state Pages nil)}
                        r/app-routing-tree)]
    #?(:clj  default-state ; the server always starts with the base UI tree, just like the client would have
       :cljs (if-let [v (ssr/get-SSR-initial-state)] ; the client starts with the server-generated db, if available
               (atom v) ; putting the state in an atom tells Om it is already normalized
               default-state)))) ; the default state is a tree, so no atom

(defui ^:once Root
  ; InitialAppState isn't here, because SSR will want to send *normalized* state, and there is no way to return that from here.
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
