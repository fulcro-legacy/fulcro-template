#!/bin/bash

echo "Building i18n"
lein cljsbuild once i18n
echo "Running extraction"
lein run -m clojure.main script/extract_strings.clj
echo "Done."
cat <<EOF
Prior translations were merged in all of your exising locales (unless you saw errors).

If you have a new locale you'd like to support, generate a new PO file for it using:

   msginit --no-translator -l LOCALE --no-wrap -o i18n/LOCALE.po -i i18n/messages.pot

Now give all of your PO files in i18n to a translator (or use PoEdit Pro to get close). Then put the
updated PO files back in i18n and run i18n-generate.sh

See the GNU Gettext manual for more instructions on working with POT and PO files.

EOF

