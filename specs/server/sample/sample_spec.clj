(ns sample.sample-spec
  (:require
    ;[untangled-spec.stub]
    [clojure.test :refer [is]]
    [untangled-spec.core :refer [specification provided behavior assertions]]))

(specification "Server Math"
  (behavior "addition computes addition correctly"
    (assertions
      "with positive integers"
      (+ 1 5 3) => 9
      "with negative integers"
      (+ -1 -3 -5) => -9
      "with a mix of signed integers"
      (+ +5 -3) => 2)))

