(ns fulcro-template.api.user-db
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as c]))

(defprotocol UserDB
  (next-id [this] "Returns the next available user ID")
  (get-user [this id] [this username password] "Get a user by their user ID")
  (add-user [this user] "Add a user to the database. UID must be a tempid. Returns a map from tempid to new real ID."))

(defrecord InMemoryUserDB [valid-users]
  c/Lifecycle
  (start [this]
    (timbre/info "Starting in-memory user database")
    (assoc this :valid-users (atom {1 {:uid 1 :name "Tony" :email "tony@nowhere.com" :password "letmein"}
                                    2 {:uid 2 :name "Joe" :email "joe@nowhere.com" :password "letmein"}})))
  (stop [this]
    (timbre/info "Stopping in-memory user database")
    this)
  UserDB
  (next-id [this] (->> @valid-users keys (reduce max) inc))
  (get-user [this id] (get @valid-users id nil))
  (get-user [this uname pword]
    (timbre/info "Login attempt for " uname)
    (let [all-users      (vals @valid-users)
          matching-entry (->> all-users
                           (filter (fn [u] (and (= 0 (.compareToIgnoreCase (:email u) uname))
                                             (= (:password u) pword))))
                           first)]
      (and matching-entry (select-keys matching-entry [:uid :email :name]))))
  (add-user [this {:keys [uid] :as user}]
    (let [real-id (next-id this)]
      (timbre/info "Adding user " real-id)
      (swap! valid-users assoc real-id (select-keys (assoc user :uid real-id) [:uid :name :email :password]))
      {uid real-id})))
