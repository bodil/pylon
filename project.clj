(defproject org.bodil/pylon "0.1.0-SNAPSHOT"
  :description "JS interop tools for Cljs"
  :url "https://github.com/bodil/pylon"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :plugins [[lein-cljsbuild "0.2.9"]
            [lein-npm "0.1.0"]
            [org.bodil/lein-noderepl "0.1.1"]]
  :cljsbuild {:builds
              [{:source-path "src"
                :compiler
                {:output-to "js/main.js"
                 :output-dir "js"
                 :optimizations :simple
                 :target :nodejs
                 :jar true}}]}
  :main "js/main.js")
