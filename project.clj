(defproject untangled-template "0.1.0-SNAPSHOT"
  :description "A clonable & upstream untangled template"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671"]

                 [awkay/untangled "1.0.0-beta2-SNAPSHOT"]
                 [org.omcljs/om "1.0.0-beta1"]
                 [kibu/pushy "0.3.7"]
                 [bidi "2.1.1"]

                 [awkay/untangled-spec "1.0.0-beta1" :scope "test"]
                 [lein-doo "0.1.7" :scope "test"]
                 [org.clojure/core.async "0.3.443"]
                 [http-kit "2.2.0"]
                 [com.taoensso/timbre "4.10.0"]]

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-doo "0.1.7"]
            [com.jakemccrary/lein-test-refresh "0.17.0"]]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  :uberjar-name "untangled_template.jar"

  :test-refresh {:report       untangled-spec.reporters.terminal/untangled-report
                 :with-repl    true
                 :changes-only true}

  :source-paths ["src/main"]
  :test-paths ["src/test"]
  :clean-targets ^{:protect false} ["target" "resources/public/js" "resources/private"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id           "production"
                        :source-paths ["src/main"]
                        :jar          true
                        :compiler     {:asset-path    "js/prod"
                                       :main          untangled-template.client-main
                                       :optimizations :advanced
                                       :output-dir    "resources/public/js/prod"
                                       :output-to     "resources/public/js/untangled_template.min.js"}}
                       {:id           "dev"
                        :figwheel     {:on-jsload "cljs.user/mount"}
                        :source-paths ["dev/client" "src/main"]
                        :compiler     {:asset-path           "js/dev"
                                       :main                 cljs.user
                                       :optimizations        :none
                                       :output-dir           "resources/public/js/dev"
                                       :output-to            "resources/public/js/untangled_template.js"
                                       :preloads             [devtools.preload]
                                       :source-map-timestamp true}}
                       {:id           "test"
                        :source-paths ["src/test" "src/main"]
                        :figwheel     {:on-jsload "untangled-template.client-test-main/client-tests"}
                        :compiler     {:asset-path    "js/test"
                                       :main          untangled-template.client-test-main
                                       :optimizations :none
                                       :output-dir    "resources/public/js/test"
                                       :output-to     "resources/public/js/test/test.js"
                                       :preloads      [devtools.preload]}}
                       {:id           "automated-tests"
                        :source-paths ["src/test" "src/main"]
                        :compiler     {:asset-path    "js/ci"
                                       :main          untangled-template.CI-runner
                                       :optimizations :none
                                       :output-dir    "resources/private/js/ci"
                                       :output-to     "resources/private/js/unit-tests.js"}}
                       {:id           "cards"
                        :figwheel     {:devcards true}
                        :source-paths ["src/main" "src/cards"]
                        :compiler     {:asset-path           "js/cards"
                                       :main                 untangled-template.cards
                                       :optimizations        :none
                                       :output-dir           "resources/public/js/cards"
                                       :output-to            "resources/public/js/cards.js"
                                       :preloads             [devtools.preload]
                                       :source-map-timestamp true}}]}

  :profiles {:uberjar {:main       untangled-template.server-main
                       :aot        :all
                       :prep-tasks ["compile"
                                    ["cljsbuild" "once" "production"]]}
             :dev     {:source-paths ["dev/client" "dev/server" "src/client" "src/server"]
                       :dependencies [[binaryage/devtools "0.9.2"]
                                      [org.clojure/tools.namespace "0.3.0-alpha3"]
                                      [com.cemerick/piggieback "0.2.1"]
                                      [figwheel-sidecar "0.5.11" :exclusions [org.clojure/tools.reader]]
                                      [devcards "0.2.3"]]
                       :repl-options {:init-ns          user
                                      :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
