(ns untangled-template.client
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            [untangled-template.api.mutations :as m]
            [untangled.client.logging :as log]))

(defonce app
  (atom (uc/new-untangled-client
          :started-callback (fn [app]
                              (f/load app :logged-in? nil {})
                              (f/load app :current-user nil {:post-mutation `m/login-complete})))))
