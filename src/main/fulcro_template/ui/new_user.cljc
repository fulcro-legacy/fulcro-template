(ns fulcro-template.ui.new-user
  (:require [fulcro.client.primitives :as prim :refer [defsc defui]]
   #?(:clj  [fulcro.client.dom-server :as dom] 
      :cljs [fulcro.client.dom :as dom]) 
            [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.i18n :refer [tr]]
            [fulcro.events :as evts]
            [fulcro.ui.forms :as f]
            [fulcro.ui.bootstrap3 :as b]
            [fulcro-template.ui.html5-routing :as r]))

(defmutation check-passwords-match
  [{:keys [form-id field kind]}]
  (action [{:keys [state]}]
    (when (and (= :password2 field) (= kind :blur))
      (let [form (get-in @state form-id)
            a    (f/current-value form :password)
            b    (f/current-value form :password2)]
        (if (and a b (pos? (.-length a)) (pos? (.-length b)) (not= a b))
          (swap! state update-in form-id assoc :ui/password-error (tr "Passwords must match"))
          (swap! state update-in form-id assoc :ui/password-error nil))))))

(defmutation create-user-failed
  [{:keys [id error]}]
  (action [{:keys [state]}]
    (swap! state assoc-in [:user/by-id id :ui/create-failed] true)))

(f/defvalidator valid-email?
  [_ value args]                                            ; crappy regex for email...but you get the point
  (seq (re-matches #"^[^ @]+@[^ ]+[.a-zA-Z0-9]*[a-zA-Z]$" value)))

(declare UserForm)

(defui ^:once UserForm
  static f/IForm
  (form-spec [this]
    [(f/id-field :uid)
     (f/text-input :name :validator `f/not-empty?)
     ; TODO: add username in use check against server
     (f/html5-input :email "email" :validator `valid-email?)
     (f/html5-input :password "password" :validator `f/minmax-length? :validator-args {:min 7 :max 100})
     (f/html5-input :password2 "password")
     (f/on-form-change `check-passwords-match)])
  static prim/InitialAppState
  (initial-state [this params] {:uid (prim/tempid) :name "" :password "" :password2 ""})
  static prim/IQuery
  (query [this] [:ui/create-failed :uid :name :email :password :password2 :ui/password-error f/form-root-key f/form-key])
  static prim/Ident
  (ident [this props] [:user/by-id (:uid props)])
  Object
  ; SSR state cannot properly initialize forms, so we ensure it is initialized on mount. This mutation is safe
  ; to run on an already initialized form, but we only do it once since the extra transact is not necessary
  ; and would be distracting in the logs.
  (componentWillReceiveProps [this props]                   ; on return to this screen once cleared
    (when (or (f/server-initialized? props) (not (f/is-form? props)))
      (prim/transact! this `[(f/initialize-form {})])))
  (componentWillMount [this]                                ; on initial from server
    (when (or (f/server-initialized? (prim/props this)) (not (f/is-form? (prim/props this))))
      (prim/transact! this `[(f/initialize-form {})])))
  (render [this]
    (let [{:keys [uid name email password password2 ui/password-error ui/create-failed] :as form} (prim/props this)
          sign-up (fn []
                    (m/set-value! this :ui/create-failed false)
                    (if (f/would-be-valid? form)
                      (f/commit-to-entity! this :remote true :fallback `create-user-failed :fallback-params {:id uid})
                      (prim/transact! this `[(f/validate-form ~{:form-id [:user/by-id uid]})])))]
      (if (prim/tempid? uid)                                ; successful submission will remap the tempid to a real ID.
        (when (f/is-form? form)                             ;prevents flicker on form init from SSR
          (b/container-fluid {}
            (b/row {}
              (b/col {:lg-offset 3 :lg 6 :xs-offset 1 :xs 11}
                (b/form-horizontal {}
                  (b/labeled-input {:split           3
                                    :error           (when (f/invalid? form :name) (tr "Please supply your name.`"))
                                    :input-generator (fn [{:keys [className]}]
                                                       (f/form-field this form :name :className className :id "username"))}
                    (tr "Name"))
                  (b/labeled-input {:split           3
                                    :error           (when (f/invalid? form :email) (tr "Must be a valid email address.`"))
                                    :input-generator (fn [{:keys [className]}]
                                                       (f/form-field this form :email :className className :id "email"))}
                    (tr "Email Address"))
                  (b/labeled-input {:split           3
                                    :error           (when (f/invalid? form :password) (tr "Password must be at least 7 characters long"))
                                    :input-generator (fn [{:keys [className]}]
                                                       (f/form-field this form :password :className className :id "password"))} (tr "Password"))
                  (b/labeled-input {:split           3
                                    :error           (when password-error password-error)
                                    :input-generator (fn [{:keys [className]}]
                                                       (f/form-field this form :password2 :className className :id "password2"
                                                         :onKeyDown (fn [evt]
                                                                      (when (evts/enter-key? evt)
                                                                        (sign-up)))))} (tr "Verify Password"))
                  (b/labeled-input {:id              "submit" :split 3
                                    :error           (when create-failed (tr "A server error happened. Please try again."))
                                    :input-generator (fn [props]
                                                       (b/button (merge props
                                                                   {:kind    :primary
                                                                    :onClick sign-up}) (tr "Sign Up!")))} ""))))))
        (b/container-fluid {}
          (b/row {}
            (b/col {:lg 6 :lg-offset 3 :xs 10 :xs-offset 1}
              (b/alert {:kind :success} (dom/span (tr "Welcome! Your account has been created. ") (dom/a {:onClick #(r/nav-to! this :login)} (tr "Please, log in.")))))))))))

(def ui-user-form (prim/factory UserForm))

(defsc NewUser [this {:keys [form]}]
  {:initial-state (fn [params] {:id :new-user :form (prim/get-initial-state UserForm {})})
   :ident         (fn [] [:new-user :page])
   :query         [:id {:form (prim/get-query UserForm)}]}
  (ui-user-form form))
