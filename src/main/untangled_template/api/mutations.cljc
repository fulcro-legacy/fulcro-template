(ns untangled-template.api.mutations
  (:require
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    #?(:cljs [untangled.client.mutations :refer [defmutation]]
       :clj
    [untangled.server :as core :refer [defmutation]])))

#?(:cljs
   (defmutation attempt-login
     "Om mutation: Attempt to log in the user. Triggers a server interaction to see if there is already a cookie."
     [{:keys [uid]}]
     (remote [env] true)
     (action [{:keys [state]}]
       (swap! state assoc
         :current-user {:id uid :name "???"}
         :server-down false))))

#?(:cljs
   (defmutation server-down
     "Om mutation: Called if the server does not respond so we can show an error."
     [p]
     (action [{:keys [state]}] (swap! state assoc :server-down true))))

#?(:cljs
   (defmutation login-complete
     "Om mutation: Attempted login post-mutation the update the UI with the result."
     [{:keys [state]} k p]
     {:action (fn []
                (let [{:keys [logged-in? current-user]} @state]
                  (if logged-in?
                    (swap! state assoc :current-page [:main :page])
                    (swap! state assoc :current-page [:login :page]))))}))

#?(:cljs
   (defmutation logout
     "Om mutation: Removes user identity from the local app and asks the server to forget the user as well."
     [p]
     (remote [env] true)
     (action [{:keys [state]}]
       (swap! state assoc
         :current-user {}
         :logged-in? false
         :current-page [:login :page]))))

#?(:clj (defonce logged-in? (atom false)))

#?(:clj
   (defmutation attempt-login
     "Server mutation: Attempt a login on the server. Returns a remapping of the user ID generated on the client."
     [{:keys [u p uid]}]
     (action [env]
       (reset! logged-in? true)
       {:uid     42
        :tempids {uid 42}})))

#?(:clj
   (defmutation logout
     "Server mutation: Log the given UI out"
     [{:keys [u p uid]}]
     (action [env] (reset! logged-in? false))))
