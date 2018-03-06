# Internationalization

This project comes with a script for extracting base strings from the source:

i18n-extract.sh - Compile and extract all strings needing translation

You take the messages.pot file to a translator and get back locale.po files. Those
all go in resources/i18n, and the server can serve them to the client via
the normal load mechanism.

See the Developer's Guide for more information on i18n.
