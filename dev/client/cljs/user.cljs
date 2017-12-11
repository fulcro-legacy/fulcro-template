(ns cljs.user
  (:require
    [fulcro.client :as fc]
    [fulcro.client.primitives :as prim]

    [fulcro-template.client :as core]
    [fulcro-template.ui.root :as root]

    [cljs.pprint :refer [pprint]]
    [fulcro.client.logging :as log]))

(enable-console-print!)

(log/set-level :all)

(defn mount []
  (reset! core/app (fc/mount @core/app root/Root "app")))

(mount)

