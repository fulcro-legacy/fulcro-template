(ns {{name}}.api.read
  (:require
    ;[untangled.datomic.protocols :as udb]
    [taoensso.timbre :as timbre]
    [{{name}}.api.mutations :as mut]))

(timbre/info "Loading API definitions for {{name}}.api.read")

(defn api-read [{:keys [query request] :as env} disp-key params]
  ;(let [connection (udb/get-connection survey-database)])
  (case disp-key
    :hello-world {:value 42}
    :logged-in? {:value @mut/logged-in?}
    :current-user {:value {:id 42 :name "Tony Kay"}}
    (throw (ex-info "Invalid request" {:query query :key disp-key}))))
