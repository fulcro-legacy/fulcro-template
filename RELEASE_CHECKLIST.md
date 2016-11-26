# RELEASE CHECKLIST

Before releasing a new version of this template, you must ensure the
following things work as expected:

- Versions
    - All deps are up-to-date
    - All tooling (lein plugins, etc) are up-to-date
    - There are NO warnings from `lein deps :tree`
- IDE Development for IntelliJ, Emacs, and VIM
    - Figwheel
    - Server run AND code reload
- Uberjar build:
    - Does a production cljs build
    - Includes all necessary resources and files
    - Is runnable according to the README instructions
    - The UI works
    - The server can be configured (e.g. PORT) as described in README
- The following figwheel configs work as expected, and hot reload:
    - CSS
    - Cards
    - General development
    - Tests
- Tests
    - Are runnable from UI (client)
    - Work with test-refresh (server)
    - Are runnable from CI (server and client)
