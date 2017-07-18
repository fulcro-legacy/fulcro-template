(ns extract-strings
  (:require [fulcro.gettext :as g]))

(g/extract-strings {:js-path "i18n/i18n.js" :po "i18n"})
(System/exit 0)
