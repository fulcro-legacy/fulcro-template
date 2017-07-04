# Template

This is a Full Stack template with specs, dev cards, and client code.
It contains a mock login/signup screen, top-level tab routing (once logged in), etc.

You must run the server (and use it through the server) for login to work, but ANY username/password are accepted. The
server always approves login.

It is set up to be deployable to Heroku (or anywhere) as a standalone jar.

## Features

The app supports a fake user database (see `valid-users` in `src/main/untangled_template/api/mutations.clj`)
with two users. HTML5 Routing is configured, and the routing tree and BIDI config
are in `html5_routing.cljs`.

The server has been set up with a session store, and the login
mutations show you how you can access and modify it (see `mutations.clj`). The
server has also been configured to serve the same index page for all URI requests
of HTML, so that the browser can decide what to show based on the URI in
app logic. See `server.clj` for details of the augmented Ring pipeline.

The HTML5 routing is smart enough to know where you wanted to go. It
remembers the URI that came in on load. If you're already logged in, it
will start the UI in the place. If you have to login, it will redirect
you to your desired page after login.

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
│       └── index.html          Production mode application home page
├── script
│   └── figwheel.clj            CLJ script for starting figwheel automatically
```

## Setting up Run Configurations (IntelliJ)

Add a figwheel config:

<img src="docs/img/figwheel.png">

Add a server config:

<img src="docs/img/server.png">

Then run *both* from IntelliJ.

## Using from other editors

See the Makefile for useful command-line targets, which are useful for
when working from a lower-level system editor.

The simplest approach is to start a REPL:

```
lein repl
```

*You will need two REPLs*: one for the server, and one for you dev builds of the client.

There is a pre-supplied function named `start-figwheel` that will start the cljs builds and figwheel hot code push.

## Using the server

IMPORTANT: When work in development mode, be sure to pass the
JVM option `-Ddev`. This will ensure the HTML5 service sends the
right page.

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

will build `target/untangled_template.jar`.

The production `prod.edn` file (in src/config) grabs the web PORT from
the environment (as required by Heroku). So, this jar can be run with:

```
export PORT=8080   # the web server port to use
java -Dconfig=config/prod.edn -jar untangled_template.jar
```

The `Procfile` gives the correct information to heroku, so if you've
configured the app (see Heroku docs) you should be able to deploy with
git:

```
git push heroku master
```
