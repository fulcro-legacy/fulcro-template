(ns untangled-template.sample-spec
  (:require
    [untangled-spec.core :refer-macros [specification behavior assertions]]))

(specification "a sample spec file"
  (behavior "for you to tinker with"
    (assertions "Silly test"
      1 => 1)))
