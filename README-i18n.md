# Internationalization

The main fulcro project has a README-i18n that is a general cheat sheet
for doing string extraction and generation. The template includes
two shell scripts that are set to do the steps for you:

i18n-extract.sh - Compile and extract all strings needing translation
i18n-generate.sh - Turn the translations into CLJC files

In between the two you need to have someone actually translate things
into the locales you want to support. Spanish translations are
already present as an example.

The project is configured to use:

`./i18n` - The location for messages.pot, LOCALE.po. You will also
see i18n.js and tmp appear here, but they are the temporary files
for extraction and are in .gitignore already.

./src/main/fulcro_template/locales - The location of the generated
CLJC locale files. These are used by Fulcro to display strings for
your supported locales, and are generated from the PO files.

See the Fulcro Dev Guide for the steps to use to internationalize your
UI.