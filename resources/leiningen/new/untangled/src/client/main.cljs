(ns {{name}}.main
  (:require [{{name}}.core :refer [app]]
            [untangled.client.core :as core]
            [{{name}}.ui.root :as root]))

(reset! app (core/mount @app root/Root "app"))
