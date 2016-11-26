LEIN_RUN = rlwrap lein run -m clojure.main ./script/figwheel.clj

# Run the dev and test cljs builds in figwheel
dev:
	lein do clean, -U deps ; JVM_OPTS="-server -Ddev -Dtest" ${LEIN_RUN}

# Run the test cljs builds in figwheel
test:
	JVM_OPTS="-server -Dtest" ${LEIN_RUN}

# Run the cards cljs builds in figwheel
cards:
	JVM_OPTS="-server -Dcards" ${LEIN_RUN}

# Run a REPL capable of running the web server
server:
	rlwrap lein run -m clojure.main

server-tests:
	lein test-refresh :run-once

# Run the command-line (karma-based) automated cljs tests
ci-cljs-tests:
	npm install
	lein do clean, doo chrome automated-tests once

# Run all tests (once) from the command line. Useful for CI
ci-tests: ci-cljs-tests server-tests

clean:
	lein clean

rename:
	bin/rename-project.sh

help:
	@ make -rpn | sed -n -e '/^$$/ { n ; /^[^ ]*:/p; }' | sort | egrep --color '^[^ ]*:'

.PHONY: dev test cards server server-tests ci-cljs-tests ci-tests clean rename help
