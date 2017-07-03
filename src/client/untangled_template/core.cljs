(ns untangled-template.core
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            [untangled-template.state.mutations :as m]
            [untangled.client.logging :as log]))

(defonce app
  (atom (uc/new-untangled-client
          :started-callback (fn [app]
                              (f/load app :logged-in? nil {})
                              (f/load app :current-user {:post-mutation `m/login-complete})))))
