(ns extract-strings
  (:require [fulcro.gettext :as g]))

(g/extract-strings {:js-path "resources/i18n/i18n.js" :po "resources/i18n"})
(System/exit 0)
