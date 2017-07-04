(ns untangled-template.client
  (:require [om.next :as om]
            [untangled.client.core :as uc]
            [untangled.client.data-fetch :as f]
            [untangled-template.api.mutations :as m]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [untangled-template.ui.html5-routing :as r]
            [untangled.client.logging :as log]
            [untangled-template.ui.user :as user]))

(defonce app
  (atom (uc/new-untangled-client
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (when @r/use-html5-routing
                                (let [root-dom   (om/app-root reconciler)
                                      ; NOTE: the :pages follow-on read, so the whole UI updates when page changes
                                      set-route! (fn [match]
                                                   (js/console.log :ROUTE match)
                                                   (om/transact! root-dom `[(r/set-route! ~match) :pages]))]
                                  (reset! r/history (pushy/pushy set-route! (partial bidi/match-route r/app-routes)))
                                  (pushy/start! @r/history)))
                              (f/load app :logged-in? nil {}) ; scalar value (boolean). No component needed.
                              (f/load app :current-user user/User {:post-mutation `m/login-complete})))))
