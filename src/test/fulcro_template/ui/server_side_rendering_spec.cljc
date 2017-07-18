(ns fulcro-template.ui.server-side-rendering-spec
  (:require
    [fulcro-template.ui.html5-routing :as r]
    [fulcro-spec.core :refer [specification provided behavior assertions when-mocking]]
    [fulcro.client.util :as util]
    [fulcro.client.core :as fc]
    [fulcro-template.ui.main :as main]
    [fulcro-template.ui.root :as root]
    ))

#?(:clj
   (specification "Server side rendering of index"
     (let [state (fc/get-initial-state root/Root {})]
       (assertions
         "Initial state for SSR starts with the default state for the ui"
         (contains? state :logged-in?) => true
         (contains? state :ui/ready?) => true
         "Includes the alternate union elements"
         :todo => true
         ))))
