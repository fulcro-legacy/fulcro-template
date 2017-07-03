(ns untangled-template.api.read
  (:require
    [untangled-template.api.mutations :as m]
    [untangled.server :refer [defquery-entity defquery-root]]))

(defquery-root :logged-in?
  "Answer the :logged-in? query"
  (value [env params]
    @m/logged-in?))

(defquery-root :logged-in?
  "Answer the :hello-world query"
  (value [env params] 42))

(defquery-root :current-user
  "Answer the :current-user query"
  (value [env params]
    {:id 42 :name "Tony Kay"}))
