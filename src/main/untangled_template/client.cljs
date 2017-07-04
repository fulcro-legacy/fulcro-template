(ns untangled-template.client
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            [untangled-template.api.mutations :as m]
            [untangled-template.ui.user :as user]))

(defonce app
  (atom (uc/new-untangled-client
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (f/load app :logged-in? nil {}) ; scalar value (boolean). No component needed.
                              (f/load app :current-user user/User {:post-mutation `m/login-complete})))))
