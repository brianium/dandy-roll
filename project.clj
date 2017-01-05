(defproject dandy "0.10-SNAPSHOT"
  :description "Watermarking in the browser"
  :url "https://github.com/brianium/dandy-roll"
  
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :global-vars {*warn-on-reflection* true}
  
  :dependencies []
  
  :plugins [[lein-figwheel "0.5.8"]
            [cider/cider-nrepl "0.14.0"]]
  
  :clean-targets ^{:protect false} [:target-path "resources/public/cljs"]
  
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/clojurescript "1.9.293"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.8"]]}}
  
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "dandy-roll.core"
                                   :output-to "resources/public/cljs/main.js"
                                   :output-dir "resources/public/cljs/out"
                                   :asset-path "cljs/out"}
                       }]})
