(ns untangled-template.main
  (:require [untangled-template.core :refer [app]]
            [untangled.client.core :as core]
            [untangled-template.ui.root :as root]))

(reset! app (core/mount @app root/Root "app"))
