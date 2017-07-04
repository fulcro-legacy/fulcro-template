(ns untangled-template.api.mutations
  (:require
    [pushy.core :as pushy]
    [untangled.client.mutations :refer [defmutation]]
    [untangled.client.routing :as ur]
    [untangled-template.ui.html5-routing :as r]
    [om.next :as om]))

(defmutation attempt-login
  "Om mutation: Attempt to log in the user. Triggers a server interaction to see if there is already a cookie."
  [{:keys [uid]}]
  (action [{:keys [state]}]
    (swap! state assoc
      :current-user {:id uid :name ""}
      :server-down false))
  (remote [env] true))

(defmutation server-down
  "Om mutation: Called if the server does not respond so we can show an error."
  [p]
  (action [{:keys [state]}] (swap! state assoc :server-down true)))

(defmutation login-complete
  "Om mutation: Attempted login post-mutation the update the UI with the result."
  [p]
  (action [{:keys [component state]}]
    ; idempotent (start routing)
    (r/start-routing (-> untangled-template.client/app deref :reconciler om/app-root))
    (let [{:keys [logged-in? current-user]} @state]
      (let [desired-page (get @state :loaded-uri (or (pushy/get-token @r/history) r/MAIN-URI))
            desired-page (if (= r/LOGIN-URI desired-page)
                           r/MAIN-URI
                           desired-page)]
        (swap! state assoc :ui/ready? true) ; Make the UI show up. (flicker prevention)
        (if logged-in?
          (if @r/use-html5-routing
            (pushy/set-token! @r/history desired-page)
            (swap! state ur/update-routing-links {:handler :main}))
          (swap! state ur/update-routing-links {:handler :login}))))))

(defmutation logout
  "Om mutation: Removes user identity from the local app and asks the server to forget the user as well."
  [p]
  (action [{:keys [state]}]
    (swap! state assoc :current-user {} :logged-in? false)
    (pushy/set-token! @r/history r/LOGIN-URI))
  (remote [env] true))

