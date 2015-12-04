(ns {{name}}.core
  (:require
    [om.next :as om]
    [untangled.client.core :as uc]
    [untangled.client.data-fetch :as df]
    {{name}}.state.mutations))

(defonce app
  (atom (uc/new-untangled-client
          :started-callback
          (fn [{:keys [reconciler]}]
            ;;TODO: initial load of data
            (df/load-data reconciler [:logged-in? :current-user]
              :post-mutation 'login/login-complete)))))
