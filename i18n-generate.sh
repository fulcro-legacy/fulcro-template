#!/bin/bash

echo "Analyzing PO files and generating CLJC..."
lein run -m clojure.main script/generate_i18n_cljc.clj
echo "Done."
