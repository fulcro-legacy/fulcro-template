(ns fulcro-template.client-main
  (:require [fulcro-template.client :refer [app]]
            [fulcro.client :as core]
            translations.es
            [fulcro-template.ui.root :as root]))

;; In dev mode, we mount from cljs/user.cljs
(reset! app (core/mount @app root/Root "app"))
