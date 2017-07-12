(ns fulcro-template.client-test-main
  (:require fulcro-template.tests-to-run
            [fulcro-spec.selectors :as sel]
            [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #"fulcro-template\..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

