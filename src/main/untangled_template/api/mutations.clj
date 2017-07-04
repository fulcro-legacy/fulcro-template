(ns untangled-template.api.mutations
  (:require
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [untangled.server :as core :refer [defmutation]]
    [untangled.server :as server]))

(def valid-users
  {1 {:uid 1 :name "Tony" :email "tony@nowhere.com" :password "letmein"}
   2 {:uid 2 :name "Joe" :email "joe@nowhere.com" :password "letmein"}})

(defn get-user
  "Returns a user matching the uname and pword, or nil"
  [uname pword]
  (timbre/info uname pword)
  (select-keys (first (filter (fn [u] (and (= 0 (.compareToIgnoreCase (:email u) uname))
                                        (= (:password u) pword))) (vals valid-users)))
    [:uid :email :name]))

(defmutation attempt-login
  "Server mutation: Attempt a login on the server. Returns a remapping of the user ID generated on the client."
  [{:keys [u p uid]}]
  (action [{:keys [request] :as env}]
    (let [{:keys [session]} request
          user     (get-user u p)
          real-uid (:uid user)]
      (if user
        (do
          (timbre/info "Logged in user " user)
          (server/augment-response {:tempids {uid real-uid}}
            (fn [resp] (assoc-in resp [:session :uid] real-uid))))
        (do
          (timbre/error "Invalid login using email: " u)
          (throw (ex-info "No such user" {})))))))

(defmutation logout
  "Server mutation: Log the given UI out"
  [ignored-params]
  ; if you wanted to directly access the session store, you can
  (action [{:keys [request session-store]}]
    (let [uid  (-> request :session :uid)
          user (get valid-users uid)]
      (timbre/info "Logout for user: " user)
      (server/augment-response {}
        (fn [resp] (assoc resp :session nil))))))
