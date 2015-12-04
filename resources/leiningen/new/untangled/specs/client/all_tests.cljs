(ns {{name}}.all-tests
  (:require
    {{name}}.tests-to-run
    [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #".*-spec")
