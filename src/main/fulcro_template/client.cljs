(ns fulcro-template.client
  (:require [om.next :as om]
            [fulcro.client.core :as uc]
            [fulcro.client.data-fetch :as f]
            [fulcro-template.api.mutations :as m]
            [fulcro-template.ui.html5-routing :as routing]
            [fulcro.client.mutations :as built-in]
            [fulcro-template.ui.root :as root]
            [fulcro-template.locales.es]
            [fulcro-template.ui.user :as user]
            [fulcro.client.logging :as log]))

(defonce app
  (atom (uc/new-fulcro-client
          :initial-state (root/initial-app-state-tree)
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (let [state (om/app-state reconciler)
                                    root  (om/app-root reconciler)
                                    {:keys [:ui/locale :ui/ready?]} @state]
                                (if ready?                  ; The only way ready is true, is if we're coming from a server-side render
                                  (routing/start-routing root)
                                  (do                       ; not SSR. we need to detect if the user is already logged in by asking the server to eval our session (cookie)
                                    (f/load app :logged-in? nil {}) ; scalar value (boolean). No component needed.
                                    (f/load app :current-user user/User {:post-mutation `m/login-complete}))))))))
