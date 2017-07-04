(ns untangled-template.sample-spec
  (:require
    [untangled-spec.core :refer [specification provided behavior assertions]]))

; These tests will run for both client and server
(specification "Server Math"
  (behavior "addition computes addition correctly"
    (assertions
      "with positive integers"
      (+ 1 5 3) => 9
      "with negative integers"
      (+ -1 -3 -5) => -9
      "with a mix of signed integers"
      (+ +5 -3) => 2)))
