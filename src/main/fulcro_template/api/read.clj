(ns fulcro-template.api.read
  (:require
    [fulcro-template.api.mutations :as m]
    [fulcro.server :refer [defquery-entity defquery-root]]
    [taoensso.timbre :as timbre]
    [fulcro-template.api.user-db :as users]))

;; SERVER READ IMPLEMENTATION. We're using `fulcro-parser`. You can either use defmulti on the multimethods
;; (see fulcro.server defmulti declarations) or the defquery-* helper macros.
(defquery-root :logged-in?
  "Answer the :logged-in? query"
  (value [{:keys [request user-db]} params]
    (let [resp (boolean (-> request :session :uid))]
      (timbre/info "Logged in? " resp)
      resp)))

(defquery-root :current-user
  "Answer the :current-user query"
  (value [{:keys [request user-db]} params]
    (let [resp (-> (users/get-user user-db (-> request :session :uid))
                 (select-keys [:uid :name :email]))]
      (timbre/info "Current user: " resp)
      resp)))
