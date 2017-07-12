(ns fulcro-template.api.mutations
  (:require
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [fulcro.server :as core :refer [defmutation]]
    [fulcro.server :as server]
    [taoensso.timbre :as log]))

(def valid-users
  (atom {1 {:uid 1 :name "Tony" :email "tony@nowhere.com" :password "letmein"}
         2 {:uid 2 :name "Joe" :email "joe@nowhere.com" :password "letmein"}}))

(defn get-user
  "Returns a user matching the uname and pword, or nil"
  [uname pword]
  (timbre/info uname pword)
  (select-keys (first (filter (fn [u] (and (= 0 (.compareToIgnoreCase (:email u) uname))
                                        (= (:password u) pword))) (vals @valid-users)))
    [:uid :email :name]))

(defn next-id
  "Get the next available ID for a user."
  []
  (->> @valid-users keys (reduce max) inc))

(defn add-user
  "Add a user to the database. UID must be a tempid. Returns a map from tempid to new real ID."
  [{:keys [uid] :as user}]
  (let [real-id (next-id)]
    (log/info "Adding user " real-id)
    (swap! valid-users assoc real-id (select-keys (assoc user :uid real-id) [:uid :name :email :password]))
    {uid real-id}))

(defn commit-new [[table id] entity]
  (log/info "Committing new " table entity)
  (case table
    :user/by-id (add-user entity)
    {}))

(defmethod core/server-mutate 'fulcro.ui.forms/commit-to-entity [env k {:keys [form/new-entities] :as p}]
  {:action (fn []
             (log/info "Commit entity: " k p)
             (when (seq new-entities)
               {:tempids (reduce (fn [remaps [k v]]
                                   (log/info "Create new " k v)
                                   (merge remaps (commit-new k v))) {} new-entities)}))})

(defmutation attempt-login
  "Server mutation: Attempt a login on the server. Returns a remapping of the user ID generated on the client.

  The client sends this when the user presses 'Login' with the username (u) and password (p). If this function
  remaps to a real ID, then the user on the client (which is tempid to start) will know the real user id. Note
  that the users real database ID is placed into the server-side session store so that future requests can access it.

  The `request` will be available in the `env` of action. This is a normal Ring request (session is under :session)."
  [{:keys [u p uid]}]
  (action [{:keys [request] :as env}]
    (let [{:keys [session]} request
          user     (get-user u p)
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
  (action [{:keys [request session-store]}]
    (let [uid  (-> request :session :uid)
          user (get @valid-users uid)]
      (timbre/info "Logout for user: " user)
      (server/augment-response {}
        (fn [resp] (assoc resp :session nil))))))
