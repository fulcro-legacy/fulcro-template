(ns untangled-template.api.mutations
  (:require

    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [untangled.server.core :as core]))

(defmulti apimutate oms/dispatch)

(defonce logged-in? (atom false))

(defmethod apimutate 'login/attempt-login [env k {:keys [u p uid]}]
  {:action (fn []
             (reset! logged-in? true)
             {:uid     42
              :tempids {uid 42}})})

(defmethod apimutate 'login/logout [env k {:keys [u p uid]}]
  {:action (fn [] (reset! logged-in? false))})
