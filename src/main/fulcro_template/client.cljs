(ns fulcro-template.client
  (:require
    [fulcro-template.api.mutations :as m]
    [fulcro-template.ui.html5-routing :as routing]
    [fulcro-template.ui.user :as user]
    [fulcro.i18n :as i18n]
    [fulcro.client :as fc]
    [fulcro.client.data-fetch :as f]
    [fulcro.client.primitives :as prim]
    [fulcro.server-render :as ssr]))

(defn message-formatter [{:keys [::i18n/localized-format-string ::i18n/locale ::i18n/format-options]}]
  (let [locale-str (name locale)
        formatter  (js/IntlMessageFormat. localized-format-string locale-str)]
    (.format formatter (clj->js format-options))))

(defonce app
  (atom (fc/new-fulcro-client
          :initial-state (when-let [v (ssr/get-SSR-initial-state)] ; the client starts with the server-generated db, if available
                           (atom v))                        ; putting the state in an atom indicates it is already normalized
          :reconciler-options {:shared    {::i18n/message-formatter message-formatter}
                               :shared-fn ::i18n/current-locale}
          :started-callback (fn [{:keys [reconciler] :as app}]
                              (let [state (prim/app-state reconciler)
                                    root  (prim/app-root reconciler)
                                    {:keys [ui/ready?]} @state]
                                (if ready?                  ; The only way ready is true, is if we're coming from a server-side render
                                  (routing/start-routing root)
                                  (f/load app :current-user user/User {:post-mutation        `m/login-complete
                                                                       :post-mutation-params {:app-root (prim/app-root reconciler)}})))))))
