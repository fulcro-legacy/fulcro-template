(ns untangled-template.api.mutations
  (:require
    #?(:clj [om.next.server :as oms])
    #?(:clj
            [taoensso.timbre :as timbre])
    #?(:cljs [untangled.client.mutations :refer [defmutation]]
       :clj
            [untangled.server :as core :refer [defmutation]])
            [untangled.client.routing :as r]))

#?(:cljs
   (defmutation attempt-login
     "Om mutation: Attempt to log in the user. Triggers a server interaction to see if there is already a cookie."
     [{:keys [uid]}]
     (action [{:keys [state]}]
       (swap! state assoc
         :current-user {:id uid :name ""}
         :server-down false))
     (remote [env] true)))

#?(:cljs
   (defmutation server-down
     "Om mutation: Called if the server does not respond so we can show an error."
     [p]
     (action [{:keys [state]}] (swap! state assoc :server-down true))))

#?(:cljs
   (defmutation login-complete
     "Om mutation: Attempted login post-mutation the update the UI with the result."
     [p]
     (action [{:keys [state]}]
       (let [{:keys [logged-in? current-user]} @state]
         (swap! state (fn [s]
                        (if logged-in?
                          (r/update-routing-links s {:handler :main})
                          (r/update-routing-links s {:handler :login}))))))))

#?(:cljs
   (defmutation logout
     "Om mutation: Removes user identity from the local app and asks the server to forget the user as well."
     [p]
     (action [{:keys [state]}]
       (swap! state (fn [s]
                      (-> s
                        (r/update-routing-links {:handler :login})
                        (assoc :current-user {} :logged-in? false)))))
     (remote [env] true)))

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
