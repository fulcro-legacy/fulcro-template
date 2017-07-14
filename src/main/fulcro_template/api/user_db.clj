(ns fulcro-template.api.user-db
  (:require [taoensso.timbre :as timbre]))

(def valid-users
  (atom {1 {:uid 1 :name "Tony" :email "tony@nowhere.com" :password "letmein"}
         2 {:uid 2 :name "Joe" :email "joe@nowhere.com" :password "letmein"}}))

(defn get-user
  "Returns a user matching the uname and pword, or nil"
  ([id] (get @valid-users id nil))
  ([uname pword]
   (timbre/info uname pword)
   (select-keys (first (filter (fn [u] (and (= 0 (.compareToIgnoreCase (:email u) uname))
                                         (= (:password u) pword))) (vals @valid-users)))
     [:uid :email :name])))

(defn next-id
  "Get the next available ID for a user."
  []
  (->> @valid-users keys (reduce max) inc))

(defn add-user
  "Add a user to the database. UID must be a tempid. Returns a map from tempid to new real ID."
  [{:keys [uid] :as user}]
  (let [real-id (next-id)]
    (timbre/info "Adding user " real-id)
    (swap! valid-users assoc real-id (select-keys (assoc user :uid real-id) [:uid :name :email :password]))
    {uid real-id}))

