(ns {{name}}.migrations.{{name}}-{{basic-date}}
  (:require [datomic.api :as d]
            [untangled.datomic.schema :as s]))

(defn transactions []
  [;(s/generate-schema (s/dbfns->datomic ...))
   (s/generate-schema
     [(s/schema thing
                (s/fields
                  [children :ref :many :component {:references :thing/type}
                   "thing can reference multiple things"]
                  [kind :enum [:foo :bar :qux]
                   "type of thing this is"]))])])
