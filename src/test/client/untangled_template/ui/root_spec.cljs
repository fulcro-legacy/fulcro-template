(ns untangled-template.ui.root-spec
  (:require
    [untangled-template.ui.root :as root]
    [untangled-spec.core :refer-macros [specification component behavior assertions]]))

(specification "Root level mutations"
  (component "Navigation - nav-to helper function"
    (let [state-atom (atom {})
          env {:state state-atom}]

      (root/nav-to env :my-page)

      (assertions
        "Sets the current-page ident to have a second element of :page"
        (-> state-atom deref :current-page second) => :page
        "Sets the current-page ident to have the selected page as the first element"
        (-> state-atom deref :current-page first) => :my-page))))
