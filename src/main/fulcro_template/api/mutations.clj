(ns fulcro-template.api.mutations
  (:require
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [fulcro.server :as core :refer [defmutation]]
    [fulcro.server :as server]
    [fulcro-template.api.user-db :as users]
    [taoensso.timbre :as log]))

(defn commit-new [user-db [table id] entity]
  (log/info "Committing new " table entity)
  (case table
    :user/by-id (users/add-user user-db entity)
    {}))

(defmethod core/server-mutate 'fulcro.ui.forms/commit-to-entity [{:keys [user-db]} k {:keys [form/new-entities] :as p}]
  {:action (fn []
             (log/info "Commit entity: " k p)
             (when (seq new-entities)
               {:tempids (reduce (fn [remaps [k v]]
                                   (log/info "Create new " k v)
                                   (merge remaps (commit-new user-db k v))) {} new-entities)}))})

(defmutation attempt-login
  "Server mutation: Attempt a login on the server. Returns a remapping of the user ID generated on the client.

  The client sends this when the user presses 'Login' with the username (u) and password (p). If this function
  remaps to a real ID, then the user on the client (which is tempid to start) will know the real user id. Note
  that the users real database ID is placed into the server-side session store so that future requests can access it.

  The `request` will be available in the `env` of action. This is a normal Ring request (session is under :session)."
  [{:keys [u p uid]}]
  (action [{:keys [request user-db] :as env}]
    (let [{:keys [session]} request
          user     (users/get-user user-db u p)
          real-uid (:uid user)]
      (Thread/sleep 300)                                    ; pretend it takes a while to auth a user
      (if user
        (do
          (timbre/info "Logged in user " user)
          (server/augment-response {:tempids {uid real-uid}}
            (fn [resp] (assoc-in resp [:session :uid] real-uid))))
        (do
          (timbre/error "Invalid login using email: " u)
          (throw (ex-info "No such user" {})))))))

(defmutation logout
  "Server mutation: Log the given UI out. This mutation just removes the session, so that the server won't recognize
  the user anymore."
  [ignored-params]
  ; if you wanted to directly access the session store, you can
  (action [{:keys [request session-store user-db]}]
    (let [uid  (-> request :session :uid)
          user (users/get-user user-db uid)]
      (timbre/info "Logout for user: " user)
      (server/augment-response {}
        (fn [resp] (assoc resp :session nil))))))
