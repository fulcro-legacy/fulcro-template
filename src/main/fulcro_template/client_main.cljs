(ns fulcro-template.client-main
  (:require [fulcro-template.client :refer [app]]
            [fulcro.client.core :as core]
            translations.es
            [fulcro-template.ui.root :as root]
            cljsjs.react.dom.server
            [om.next :as om]))

;; In dev mode, we mount from cljs/user.cljs
(when-not (exists? js/usingNashorn)
  (reset! app (core/mount @app root/Root "app")))


