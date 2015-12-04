(ns {{name}}.api.mutations
  (:require
    {{#when-datomic}}[datomic.api :as d]{{/when-datomic}}
    [om.next.server :as oms]
    [taoensso.timbre :as timbre]
    [untangled.server.core :as core]))

(defmulti apimutate oms/dispatch)

(defonce logged-in? (atom false))

(defmethod apimutate 'login/attempt-login [env k {:keys [u p uid]}]
  {:action (fn []
             (reset! logged-in? true)
             {:tempids {uid 42}})})

(defmethod apimutate 'login/logout [env k {:keys [u p uid]}]
  {:action (fn [] (reset! logged-in? false))})
