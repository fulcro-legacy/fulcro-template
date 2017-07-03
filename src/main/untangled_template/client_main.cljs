(ns untangled-template.client-main
  (:require [untangled-template.client :refer [app]]
            [untangled.client.core :as core]
            [untangled-template.ui.root :as root]))

(reset! app (core/mount @app root/Root "app"))
