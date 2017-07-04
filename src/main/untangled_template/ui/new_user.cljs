(ns untangled-template.ui.new-user
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as u]
            [om.dom :as dom]
            [untangled.client.mutations :as m :refer [defmutation]]
            [untangled.i18n :refer [tr]]
            [untangled.ui.forms :as f]
            [untangled.ui.bootstrap3 :as b]
            [untangled.client.core :as uc]))

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

(f/defvalidator valid-email?
  [_ value args] ; crappy regex for email...but you get the point
  (seq (re-matches #"^[^ @]+@[^ ]+[.a-zA-Z0-9]*[a-zA-Z]$" value)))

(defui ^:once UserForm
  static f/IForm
  (form-spec [this]
    [(f/id-field :uid)
     (f/on-form-change `check-passwords-match)
     ; TODO: add validation, including username in use check against server
     (f/html5-input :name "email" :validator `valid-email?)
     (f/html5-input :password "password" :validator `f/minmax-length? :validator-args {:min 7 :max 100})
     (f/html5-input :password2 "password")])
  static u/InitialAppState
  (initial-state [this params] (f/build-form UserForm {:uid (om/tempid) :name "" :password "" :password2 ""}))
  static om/IQuery
  (query [this] [:uid :name :password :password2 :ui/password-error f/form-root-key f/form-key])
  static om/Ident
  (ident [this props] [:user/by-id (:uid props)])
  Object
  (render [this]
    (let [{:keys [uid name password password2 ui/password-error] :as form} (om/props this)
          sign-up (fn [] (if (f/would-be-valid? form)
                           (f/commit-to-entity! this :remote true)
                           (om/transact! this `[(f/validate-form ~{:form-id [:user/by-id uid]})])))]
      (b/container-fluid {}
        (b/row {}
          (b/col {:lg-offset 4 :lg 4 :xs-offset 1 :xs 11}
            (b/form-horizontal {}
              (b/labeled-input {:split           2
                                :error           (when (f/invalid? form :name) (tr "Must be a valid email address.`"))
                                :input-generator (fn [{:keys [className]}]
                                                   (f/form-field this form :name :className className :id "username"))}
                (tr "Email Address"))
              (b/labeled-input {:split           2
                                :error           (when (f/invalid? form :password) (tr "Password must be at least 7 characters long"))
                                :input-generator (fn [{:keys [className]}]
                                                   (f/form-field this form :password :className className :id "password"))} (tr "Password"))
              (b/labeled-input {:split           2
                                :error           (when password-error password-error)
                                :input-generator (fn [{:keys [className]}]
                                                   (f/form-field this form :password2 :className className :id "password2"))} (tr "Verify Password"))
              (b/labeled-input {:id              "submit" :split 2
                                :input-generator (fn [props]
                                                   (b/button (merge props
                                                               {:kind    :primary
                                                                :onClick sign-up}) (tr "Sign Up!")))} ""))))))))

(def ui-user-form (om/factory UserForm))

(defui ^:once NewUser
  static u/InitialAppState
  (initial-state [this params] {:id :new-user :form (uc/get-initial-state UserForm {})})
  static om/IQuery
  (query [this] [:id {:form (om/get-query UserForm)}])
  Object
  (render [this]
    (let [{:keys [form]} (om/props this)]
      (ui-user-form form))))
