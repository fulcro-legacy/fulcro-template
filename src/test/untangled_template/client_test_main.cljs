(ns untangled-template.client-test-main
  (:require untangled-template.tests-to-run
            [untangled-spec.selectors :as sel]
            [untangled-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #"untangled-template\..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

