(ns untangled-template.client-main
  (:require [untangled-template.client :refer [app]]
            [untangled.client.core :as core]
            [untangled-template.ui.root :as root]))

;; In dev mode, we mount from cljs/user.cljs
(reset! app (core/mount @app root/Root "app"))
