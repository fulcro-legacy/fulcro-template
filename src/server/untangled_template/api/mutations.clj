(ns untangled-template.api.mutations
  (:require
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [untangled.server :as core :refer [defmutation]]))

(defonce logged-in? (atom false))

(defmutation attempt-login
  "Server mutation: Attempt a login on the server. Returns a remapping of the user ID generated on the client."
  [{:keys [u p uid]}]
  (action [env]
    (reset! logged-in? true)
    {:uid     42
     :tempids {uid 42}}))

(defmutation logout
  "Server mutation: Log the given UI out"
  [{:keys [u p uid]}]
  (action [env] (reset! logged-in? false)))
