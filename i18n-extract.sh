#!/bin/bash

echo "Building i18n"
lein cljsbuild once i18n
echo "Running extraction"
xgettext --from-code=UTF-8 --debug -k -ktr_alpha:1 -ktrc_alpha:1c,2 -ktrf_alpha:1 -o resources/i18n/messages.pot resources/i18n/i18n.js
echo "Done."
cat <<EOF
You should msgmerge to merge the new translations in messages.pot into your existing translation files. For example:

   $ cd resources/i18n
   $ for f in *.po
   do
     msgmerge --force-po --no-wrap -U  $f messages.pot
   done

If you have a new locale you'd like to support, generate a new PO file for it using:

   msginit --no-translator -l LOCALE --no-wrap -o resources/i18n/LOCALE.po -i resources/i18n/messages.pot

Now give all of your PO files in i18n to a translator (or use PoEdit Pro to get close). Then put the
updated PO files back in i18n and run i18n-generate.sh

See the GNU Gettext manual for more instructions on working with POT and PO files.

EOF

