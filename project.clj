(defproject brianium/dandy-roll "0.13"
  :description "Watermarking in the browser"
  :url "https://github.com/brianium/dandy-roll"
  
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :global-vars {*warn-on-reflection* true}
  
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]]
  
  :plugins [[lein-figwheel "0.5.8"]
            [lein-doo "0.1.7"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]
  
  :clean-targets ^{:protect false} [:target-path "resources/public/cljs"]
  
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.8"]
                                  [lein-doo "0.1.7"]
                                  [devcards "0.2.2"]]
                   :plugins [;[cider/cider-nrepl "0.14.0"]
                             ]}}
  
  :jar-exclusions [#"test" #"resources" #"public"]

  :source-paths ["src"]
  
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "dandy-roll.core"
                                   :output-to "resources/public/cljs/main.js"
                                   :output-dir "resources/public/cljs/out"
                                   :asset-path "cljs/out"}}

                       {:id "devcards-test"
                        :source-paths ["src" "test"]
                        :figwheel {:devcards true}
                        :compiler {:main runners.browser
                                   :optimizations :none
                                   :asset-path "cljs/tests/out"
                                   :output-dir "resources/public/cljs/tests/out"
                                   :output-to "resources/public/cljs/tests/all-tests.js"
                                   :source-map-timestamp true}}
                       
                       {:id "devcards-standalone"
                        :source-paths ["src" "test"]
                        :compiler {:main runners.browser
                                   :devcards true
                                   :optimizations :advanced
                                   :asset-path "cljs/out"
                                   :output-to "resources/public/dandy-roll.js"}}]
              })
