(ns untangled-template.tests-to-run
  (:require
    untangled-template.ui.html5-routing-spec
    untangled-template.sample-spec))

;; This file is used as a place to require all client tests so they get loaded by BOTH the CI runner and the
;; development-time main. All of your cljs and cljc specifications should be required at the top.
