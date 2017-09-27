(ns fulcro-template.client-nashorn-rendering
  (:require [om.next :as om]
            [fulcro-template.ui.root :as root]
            [fulcro.client.core :as fc]
            [fulcro.client.logging :as log]
            [fulcro.client.util :as util]))

(def ui-root (om/factory root/Root))

(defn ^:export server-render [props-str]
  (if-let [props (some-> props-str util/transit-str->clj)]
    (js/ReactDOMServer.renderToString (ui-root props))
    (js/ReactDOMServer.renderToString (ui-root (fc/get-initial-state root/Root nil)))))
