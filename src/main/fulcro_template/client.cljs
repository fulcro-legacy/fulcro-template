(ns fulcro-template.client
  (:require [om.next :as om]
            [fulcro.client.core :as uc]
            [fulcro.client.data-fetch :as f]
            [fulcro-template.api.mutations :as m]
            [fulcro-template.ui.user :as user]))

(defonce app
  (atom (uc/new-fulcro-client
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (f/load app :logged-in? nil {}) ; scalar value (boolean). No component needed.
                              (f/load app :current-user user/User {:post-mutation `m/login-complete})))))
