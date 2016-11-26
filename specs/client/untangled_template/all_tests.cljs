(ns untangled-template.all-tests
  (:require
    untangled-template.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #".*-spec")
