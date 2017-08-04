(defproject fulcrologic/fulcro-template "0.1.0-SNAPSHOT"
  :description "A clonable & upstream fulcro template"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.671"]
                 [fulcrologic/fulcro "1.0.0-beta7-SNAPSHOT" :exclusions [commons-codec]]
                 [org.clojure/core.async "0.3.443"]
                 [org.omcljs/om "1.0.0-beta1"]
                 [kibu/pushy "0.3.7"]
                 [bidi "2.1.2"]
                 [ring/ring-core "1.6.2" :exclusions [commons-codec]]
                 [bk/ring-gzip "0.2.1"]
                 [http-kit "2.2.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [fulcrologic/fulcro-spec "1.0.0-beta5" :scope "test" :exclusions [commons-codec fulcrologic/fulcro commons-fileupload]]]

  :uberjar-name "fulcro_template.jar"

  :source-paths ["src/main"]
  :test-paths ["src/test"]
  :clean-targets ^{:protect false} ["target" "resources/public/js" "resources/private"]

  ; Notes  on production build:
  ; 1. Server-side rendering does not know about name mangling of the INITIAL_APP_STATE variable. That is why
  ; there is an externs.js. It tells Closure not to rename that variable.
  ; 2. The hot code reload stuff in the dev profile WILL BREAK ADV COMPILATION. So, make sure you
  ; use `lein with-profile production cljsbuild once production` to build!
  :cljsbuild {:builds [{:id           "production"
                        :source-paths ["src/main"]
                        :jar          true
                        :compiler     {:asset-path    "js/prod"
                                       :main          fulcro-template.client-main
                                       :optimizations :advanced ; dead code elim is killing queries on components
                                       :externs       ["externs.js"]
                                       :source-map    "resources/public/js/fulcro_template.js.map"
                                       :output-dir    "resources/public/js/prod"
                                       :output-to     "resources/public/js/fulcro_template.js"}}]}

  :profiles {:uberjar    {:main           fulcro-template.server-main
                          :aot            :all
                          :jar-exclusions [#"public/js/prod" #"com/google.*js$"]
                          :prep-tasks     ["clean"
                                           ["clean"]
                                           "compile"
                                           ["with-profile" "production" "cljsbuild" "once" "production"]]}
             :production {}
             :dev        {:source-paths ["dev/client" "dev/server" "src/main" "src/test" "src/cards"]

                          :jvm-opts     ["-XX:-OmitStackTraceInFastThrow" "-client" "-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"
                                         "-Xmx1g" "-XX:+UseConcMarkSweepGC" "-XX:+CMSClassUnloadingEnabled" "-Xverify:none"]

                          :doo          {:build "automated-tests"
                                         :paths {:karma "node_modules/karma/bin/karma"}}

                          :figwheel     {:css-dirs ["resources/public/css"]}

                          :test-refresh {:report       fulcro-spec.reporters.terminal/fulcro-report
                                         :with-repl    true
                                         :changes-only true}

                          :cljsbuild    {:builds
                                         [{:id           "dev"
                                           :figwheel     {:on-jsload "cljs.user/mount"}
                                           :source-paths ["dev/client" "src/main"]
                                           :compiler     {:asset-path           "js/dev"
                                                          :main                 cljs.user
                                                          :optimizations        :none
                                                          :output-dir           "resources/public/js/dev"
                                                          :output-to            "resources/public/js/fulcro_template.js"
                                                          :preloads             [devtools.preload]
                                                          :source-map-timestamp true}}
                                          {:id           "i18n" ;for gettext string extraction
                                           :source-paths ["src/main"]
                                           :compiler     {:asset-path    "i18n"
                                                          :main          fulcro-template.client-main
                                                          :optimizations :whitespace
                                                          :output-dir    "i18n/tmp"
                                                          :output-to     "i18n/i18n.js"}}
                                          {:id           "test"
                                           :source-paths ["src/test" "src/main"]
                                           :figwheel     {:on-jsload "fulcro-template.client-test-main/client-tests"}
                                           :compiler     {:asset-path    "js/test"
                                                          :main          fulcro-template.client-test-main
                                                          :optimizations :none
                                                          :output-dir    "resources/public/js/test"
                                                          :output-to     "resources/public/js/test/test.js"
                                                          :preloads      [devtools.preload]}}
                                          {:id           "automated-tests"
                                           :source-paths ["src/test" "src/main"]
                                           :compiler     {:asset-path    "js/ci"
                                                          :main          fulcro-template.CI-runner
                                                          :optimizations :none
                                                          :output-dir    "resources/private/js/ci"
                                                          :output-to     "resources/private/js/unit-tests.js"}}
                                          {:id           "cards"
                                           :figwheel     {:devcards true}
                                           :source-paths ["src/main" "src/cards"]
                                           :compiler     {:asset-path           "js/cards"
                                                          :main                 fulcro-template.cards
                                                          :optimizations        :none
                                                          :output-dir           "resources/public/js/cards"
                                                          :output-to            "resources/public/js/cards.js"
                                                          :preloads             [devtools.preload]
                                                          :source-map-timestamp true}}]}

                          :plugins      [[lein-cljsbuild "1.1.6"]
                                         [lein-doo "0.1.7"]
                                         [com.jakemccrary/lein-test-refresh "0.17.0"]]

                          :dependencies [[binaryage/devtools "0.9.4"]
                                         [org.clojure/tools.namespace "0.3.0-alpha4"]
                                         [org.clojure/tools.nrepl "0.2.13"]
                                         [com.cemerick/piggieback "0.2.2"]
                                         [lein-doo "0.1.7" :scope "test"]
                                         [figwheel-sidecar "0.5.11" :exclusions [org.clojure/tools.reader]]
                                         [devcards "0.2.3"]]
                          :repl-options {:init-ns          user
                                         :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
