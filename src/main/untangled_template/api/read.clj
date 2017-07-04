(ns untangled-template.api.read
  (:require
    [untangled-template.api.mutations :as m]
    [untangled.server :refer [defquery-entity defquery-root]]
    [taoensso.timbre :as timbre]))

;; SERVER READ IMPLEMENTATION. We're using `untangled-parser`. You can either use defmulti on the multimethods
;; (see untangled.server defmulti declarations) or the defquery-* helper macros.
(defquery-root :logged-in?
  "Answer the :logged-in? query"
  (value [{:keys [request]} params]
    (let [resp (boolean (-> request :session :uid))]
      (timbre/info "Logged in? " resp)
      resp)))

(defquery-root :current-user
  "Answer the :current-user query"
  (value [{:keys [request]} params]
    (let [resp (-> m/valid-users
                 (get (-> request :session :uid))
                 (select-keys [:uid :name :email]))]
      (timbre/info "Current user: " resp)
      resp)))
