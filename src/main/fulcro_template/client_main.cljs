(ns fulcro-template.client-main
  (:require [fulcro-template.client :refer [app]]
            [fulcro.client.core :as core]
            translations.es
            [fulcro-template.ui.root :as root]
            cljsjs.react.dom.server
            [om.next :as om]))

;; In dev mode, we mount from cljs/user.cljs
;(reset! app (core/mount @app root/Root "app"))

(def ui-root (om/factory root/Root))

(defn ^:export server-render []
  (js/ReactDOMServer.renderToString (ui-root (core/get-initial-state root/Root nil))))
