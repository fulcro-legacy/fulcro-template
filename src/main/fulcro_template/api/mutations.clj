(ns fulcro-template.api.mutations
  (:require
    [fulcro.server :as oms]
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





(defmutation logout
  "Server mutation: Log the given UI out. This mutation just removes the session, so that the server won't recognize
  the user anymore."
  [ignored-params]
  ; if you wanted to directly access the session store, you can
  (action [{:keys [request session-store user-db]}]
    (let [uid  (-> request :session :uid)
          user (users/get-user user-db uid)]
      (timbre/info "Logout for user: " uid)
      (server/augment-response {}
        (fn [resp] (assoc resp :session nil))))))
