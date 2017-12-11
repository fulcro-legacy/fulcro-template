(ns fulcro-template.ui.server-side-rendering-spec
  (:require
    [fulcro-spec.core :refer [specification provided behavior assertions when-mocking]]
    [fulcro-template.ui.root :as root]
    [fulcro.server-render :as ssr]
    [fulcro.client.primitives :as prim]))

#?(:clj
   (specification "Server side rendering of index"
     (let [state (ssr/build-initial-state (prim/get-initial-state root/Root nil) root/Root)]
       (assertions
         "Initial state for SSR starts with the default state for the ui"
         (contains? state :logged-in?) => true
         (contains? state :ui/ready?) => true))))
