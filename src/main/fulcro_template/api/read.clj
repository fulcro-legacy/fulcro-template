(ns fulcro-template.api.read
  (:require
    [fulcro-template.api.mutations :as m]
    [fulcro.server :refer [defquery-entity defquery-root]]
    [taoensso.timbre :as timbre]
    [fulcro-template.api.user-db :as users]
    [fulcro.server :as server]))

;; SERVER READ IMPLEMENTATION. We're using `fulcro-parser`. You can either use defmulti on the multimethods
;; (see fulcro.server defmulti declarations) or the defquery-* helper macros.
(defn attempt-login
  "The `request` will be available in the `env` of action. This is a normal Ring request (session is under :session)."
  [user-db request username password]
  (let [user        (users/get-user user-db username password)
        real-uid    (:uid user)
        secure-user (select-keys user [:name :uid :email])]
    (Thread/sleep 300)                                      ; pretend it takes a while to auth a user
    (if user
      (do
        (timbre/info "Logged in user " user)
        (server/augment-response secure-user
          (fn [resp] (assoc-in resp [:session :uid] real-uid))))
      (do
        (timbre/error "Invalid login using email: " username)
        {:uid :no-such-user :name "Unkown" :email username :login/error "Invalid credentials"}))))

(defquery-root :current-user
  "Answer the :current-user query. This is also how you log in (passing optional username and password)"
  (value [{:keys [request user-db]} {:keys [username password]}]
    (if (and username password)
      (attempt-login user-db request username password)
      (let [resp (-> (users/get-user user-db (-> request :session :uid))
                   (select-keys [:uid :name :email]))]
        (timbre/info "Current user: " resp)
        resp))))
