# Template

This is a Full Stack template with specs, dev cards, and client code.
It contains a mock login/signup screen, top-level tab routing (once logged in), etc.

You must run the server (and use it through the server) for login to work, but ANY username/password are accepted. The
server always approves login.

It is set up to be deployable to Heroku (or anywhere) as a standalone jar.

## Contents

```
├── Makefile                    Convenience targets
├── Procfile                    Sample Heroku deployment file
├── dev
│   ├── client
│   │   └── cljs
│   │       └── user.cljs       REPL helpers and entry point for cljs dev mode
│   └── server
│       └── user.clj            REPL functions for starting server and cljs builds
├── package.json                NODE config, used for running CI cljs tests
├── project.clj
├── resources
│   └── public
│       ├── cards.html          Devcards HTML page
│       ├── css
│       │   ├── edn.css         CSS files for rendering specs in browser
│       │   └── test.css
│       ├── index-dev.html      Dev mode application home page
│       ├── index.html          Production mode application home page
│       └── test.html           Tests HTML page
├── script
│   └── figwheel.clj            CLJ script for starting figwheel automatically
├── specs
│   ├── client
│   │   └── {{sanitized}}
│   │       ├── all_tests.cljs       CI file for running all tests
│   │       ├── sample_spec.cljs     Sample CLJS specification
│   │       ├── spec_main.cljs       File to join all specs into a browser-runnable spec
│   │       ├── tests_to_run.cljs    Common file (for CI and Browser) to ensure all tests are loaded
│   │       └── ui
│   │           └── root_spec.cljs   Sample Specification
│   ├── config
│   └── server
│       └── sample
│           └── sample_spec.clj      Sample Server-side specification
├── src
│   ├── cards
│   │   └── {{sanitized}}
│   │       ├── cards.cljs           Devcards setup
│   │       └── intro.cljs           Sample Devcard
│   ├── client
│   │   └── {{sanitized}}
│   │       ├── core.cljs            Definition of app. Used by production and dev modes
│   │       ├── main.cljs            Production entry point for cljs app
│   │       ├── state
│   │       │   └── mutations.cljs   A place to put Om mutations
│   │       └── ui
│   │           ├── components.cljs  Sample UI component
│   │           ├── login.cljs       UI Login screen. Includes some mutations.
│   │           ├── main.cljs        UI Main screen
│   │           ├── new_user.cljs    UI New User Screen
│   │           └── root.cljs        Root UI with Union query for tab switching. Includes nav mutations.
│   └── server
│       ├── config                   Server EDN configuration files
│       │   ├── defaults.edn         Always applied (but always used as a base for config merge)
│       │   ├── dev.edn              Dev-mode config (auto-selected by user.clj setup)
│       │   └── prod.edn             Production-mode config. Selected via -Dconfig=config/prod.edn
│       └── {{sanitized}}
│           ├── api
│           │   ├── mutations.clj    Server-side Om mutations
│           │   └── read.clj         Server-side Om queries
│           ├── core.clj             Server-side entry point for production mode
│           └── system.clj           Server-side system configuration (shared for dev and production)
```

## Setting up Run Configurations (IntelliJ)

Add a figwheel config:

<img src="/docs/img/figwheel.png">

Add a server config:

<img src="/docs/img/server.png">

Then run both from IntelliJ.

## Using from other editors

See the Makefile for useful command-line targets, which are useful for
when working from a lower-level system editor.

The simplest approach is to start a REPL:

```
lein repl
```

and use the `start-figwheel` or `go` functions to start figwheel or the server.

## Using the server

In the server REPL, start the server with:

```
(go)
```

To reload the server code:

```
(reset)
```

IF your compile fails, Recompile after failed compile:

```
(refresh)
(go)
```

If you cannot find `refresh`, try:

```
(tools-ns/refresh)
```

## Using the Full Stack App (dev mode)

Open a browser on:

```
http://localhost:3000/index-dev.html
```

## Dev Cards

Open a browser on:

```
http://localhost:3449/cards.html
```

## Specs

Open a browser on:

```
http://localhost:3449/test.html
```

## Continuous Integration Tests

The project is set up to be able to run both the UI and Server tests from a
standard *NIX command-line (untested with Windows, but works with OSX and
Linux).

The UI tests use node, karma, and doo to accomplish tests.

The Makefile has targets for running the various CI tests modes. You
must install Node and NPM. In OSX, Home Brew can make quick work of that.

## Makefile

There is a GNU `Makefile` in the project that can start various command
line interactions. This file is commented so you can see what targets
are valid.

Example: Run a REPL that is ready to run the Untangled Server:

```
make server
```

# Deploying

Build the standalone Jar with:

```
lein uberjar
```

will build `target/{{name}}.jar`.

The production `prod.edn` file (in src/config) grabs the web PORT from
the environment (as required by Heroku). So, this jar can be run with:

```
export PORT=8080   # the web server port to use
java -Dconfig=config/prod.edn -jar template.jar
```

The `Procfile` gives the correct information to heroku, so if you've
configured the app (see Heroku docs) you should be able to deploy with
git:

```
git push heroku master
```
