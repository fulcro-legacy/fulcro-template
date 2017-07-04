(ns untangled-template.CI-runner
  (:require
    untangled-template.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

;; This file is for running JS tests via karma/node for CI server
(doo-all-tests #".*-spec")
