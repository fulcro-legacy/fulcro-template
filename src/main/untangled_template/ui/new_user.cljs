(ns untangled-template.ui.new-user
  (:require [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as u]
            [om.dom :as dom]
            [untangled.client.mutations :as m :refer [defmutation]]

            [untangled.i18n :refer [tr]]
            [untangled.events :as evts]
            [untangled.ui.forms :as f]
            [untangled.ui.bootstrap3 :as b]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as df]
            [untangled-template.ui.html5-routing :as r]))

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
  static u/InitialAppState
  (initial-state [this params] (f/build-form UserForm {:uid (om/tempid) :name "" :password "" :password2 ""}))
  static om/IQuery
  (query [this] [:ui/create-failed :uid :name :email :password :password2 :ui/password-error f/form-root-key f/form-key])
  static om/Ident
  (ident [this props] [:user/by-id (:uid props)])
  Object
  (render [this]
    (let [{:keys [uid name email password password2 ui/password-error ui/create-failed] :as form} (om/props this)
          sign-up (fn []
                    (m/set-value! this :ui/create-failed false)
                    (if (f/would-be-valid? form)
                      (f/commit-to-entity! this :remote true :fallback `create-user-failed :fallback-params {:id uid})
                      (om/transact! this `[(f/validate-form ~{:form-id [:user/by-id uid]})])))]
      (if (om/tempid? uid)                                  ; successful submission will remap the tempid to a real ID.
        (b/container-fluid {}
          (b/row {}
            (b/col {:lg-offset 4 :lg 4 :xs-offset 1 :xs 11}
              (b/form-horizontal {}
                (b/labeled-input {:split           2
                                  :error           (when (f/invalid? form :name) (tr "Please supply your name.`"))
                                  :input-generator (fn [{:keys [className]}]
                                                     (f/form-field this form :name :className className :id "username"))}
                  (tr "Name"))
                (b/labeled-input {:split           2
                                  :error           (when (f/invalid? form :email) (tr "Must be a valid email address.`"))
                                  :input-generator (fn [{:keys [className]}]
                                                     (f/form-field this form :email :className className :id "email"))}
                  (tr "Email Address"))
                (b/labeled-input {:split           2
                                  :error           (when (f/invalid? form :password) (tr "Password must be at least 7 characters long"))
                                  :input-generator (fn [{:keys [className]}]
                                                     (f/form-field this form :password :className className :id "password"))} (tr "Password"))
                (b/labeled-input {:split           2
                                  :error           (when password-error password-error)
                                  :input-generator (fn [{:keys [className]}]
                                                     (f/form-field this form :password2 :className className :id "password2"
                                                       :onKeyDown (fn [evt]
                                                                    (js/console.log :evt evt)
                                                                    (when (evts/enter-key? evt)
                                                                      (sign-up)))))} (tr "Verify Password"))
                (b/labeled-input {:id              "submit" :split 2
                                  :error           (when create-failed (tr "A server error happened. Please try again."))
                                  :input-generator (fn [props]
                                                     (b/button (merge props
                                                                 {:kind    :primary
                                                                  :onClick sign-up}) (tr "Sign Up!")))} "")))))
        (b/container-fluid {}
          (b/row {}
            (b/col {:lg 6 :lg-offset 3 :xs 10 :xs-offset 1}
              (b/alert {:kind :success} (dom/span nil "Welcome! Your account has been created. You can now " (dom/a #js {:onClick #(r/nav-to! this :login)} "log in."))))))))))

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
