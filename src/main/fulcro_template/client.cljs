(ns fulcro-template.client
  (:require [om.next :as om]
            [fulcro.client.core :as uc]
            [fulcro.client.data-fetch :as f]
            [fulcro-template.api.mutations :as m]
            [fulcro-template.ui.html5-routing :as routing]
            [fulcro-template.ui.user :as user]
            [fulcro.client.mutations :as built-in]
            [fulcro-template.ui.root :as root]))

(defonce app
  (atom (uc/new-fulcro-client
          :initial-state (root/initial-app-state-tree)
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (let [state      (om/app-state reconciler)
                                    root       (om/app-root reconciler)
                                    logged-in? (:logged-in? @state)]
                                (if logged-in?
                                  (routing/start-routing root)
                                  (do
                                    (f/load app :logged-in? nil {}) ; scalar value (boolean). No component needed.
                                    (f/load app :current-user user/User {:post-mutation `m/login-complete}))))))))
