(ns {{name}}.ui.root
  (:require
    [om.dom :as dom]
    [om.next :as om :refer-macros [defui]]
    [untangled.client.mutations :as mut]
    [untangled.client.core :as uc]

    [{{name}}.ui.login :as l]
    [{{name}}.ui.main :as main]
    [{{name}}.ui.new-user :as nu]))

(defn nav-to [env page] (swap! (:state env) assoc :current-page [page :page]))

(defmethod mut/mutate 'nav/new-user [env k p] {:action (fn [] (nav-to env :new-user))})
(defmethod mut/mutate 'nav/login [env k p] {:action (fn [] (nav-to env :login))})
(defmethod mut/mutate 'nav/main [env k p] {:action (fn [] (nav-to env :main))})

(defui ^:once Loading
  static uc/InitialAppState
  (initial-state [this params] {:id :loading})
  static om/IQuery
  (query [this] [:id])
  static om/Ident
  (ident [this props] [:loading :page])
  Object
  (render [this]
    (dom/div nil "Loading...")))

(def ui-loading (om/factory Loading))

(defui ^:once Pages
  static uc/InitialAppState
  (initial-state [this params] (uc/initial-state Loading nil))
  static om/IQuery
  (query [this] {:loading  (om/get-query Loading)
                 :new-user (om/get-query nu/NewUser)
                 :login    (om/get-query l/LoginPage)
                 :main     (om/get-query main/MainPage)})
  static om/Ident
  (ident [this props] [(:id props) :page])
  Object
  (render [this]
    (let [{:keys [id login] :as props} (om/props this)]
      (case id
        :new-user (nu/ui-new-user props)
        :loading (ui-loading props)
        :login (l/ui-login props)
        :main (main/ui-main props)
        (dom/div nil "MISSING PAGE")))))

(def ui-pages (om/factory Pages))

(defn ui-login-stats [loading? user logout-fn]
  (dom/p #js {:className "navbar-text navbar-right"}
    (when loading?
      (dom/span #js {:className "badge"} "..."))
    (:name user)
    (dom/br nil)
    (dom/a #js {:onClick logout-fn} " Log out")))

(defn ui-login-button [loading? login-fn]
  (dom/p #js {:className "navbar-right"}
    (when loading?
      (dom/span #js {:className "navbar-text badge"} "..."))
    (dom/button #js {:type      "button"
                     :onClick   login-fn
                     :className "btn btn-default navbar-btn "}
      "Sign in")))

(defn ui-navbar [this]
  (let [login #(om/transact! this '[(nav/login)])
        logout #(om/transact! this '[(login/logout)])
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
              (dom/li nil (dom/a #js {:className "active" :onClick #(om/transact! this '[(nav/main)]) :href "#"} "Main"))))
          (if logged-in?
            (ui-login-stats loading-data current-user logout)
            (ui-login-button loading-data login)))))))

(defui ^:once Root
  static om/IQuery
  (query [this] [:ui/react-key :logged-in? :current-user :ui/loading-data {:current-page (om/get-query Pages)}])
  static uc/InitialAppState
  (initial-state [this params] {:logged-in? false :current-user {} :current-page (uc/initial-state Pages nil)})
  Object
  (render [this]
    (let [{:keys [ui/loading-data ui/react-key current-page current-user logged-in?] :or {ui/react-key "ROOT"}} (om/props this)
          logout #(om/transact! this '[(login/logout)])]
      (dom/div #js {:key react-key}
        (ui-navbar this)
        (ui-pages (om/computed current-page {:logout logout}))))))
