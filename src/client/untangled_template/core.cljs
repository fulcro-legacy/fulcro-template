(ns untangled-template.core
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            untangled-template.state.mutations
            [untangled.client.logging :as log]))

(defn merge-mutations [state k p]
  (log/info "Got return value for " k " -> " p)
  state)

(defonce app
         (atom (uc/new-untangled-client
                 :mutation-merge merge-mutations
                 :started-callback (fn [{:keys [reconciler]}]
                                     (f/load-data reconciler [:logged-in? :current-user]
                                                  :post-mutation 'login/login-complete)
                                     ;;TODO: initial load of data
                                     ))))
