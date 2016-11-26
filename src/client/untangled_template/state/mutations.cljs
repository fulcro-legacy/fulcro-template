(ns untangled-template.state.mutations
  (:require [om.next :as om]
            [untangled.client.mutations :refer [mutate post-mutate]]
            [untangled.client.impl.data-fetch :as df]))

(comment
  (defmethod mutate 'app/do-thing [{:keys [state ast] :as env} mut-name params]
    {:remote true; or modify (env :ast)
     :action (fn [] (swap! state assoc :thing params))}))
