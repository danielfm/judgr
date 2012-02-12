(defproject clj-thatfinger "0.0.1"
  :description "Simple Naïve Bayes Classifier implementation that flags offensive messages"
  :url "http://github.com/danielfm/clj-thatfinger"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.macro "0.1.1"]
                 [noir "1.2.1"]
                 [congomongo "0.1.7"]
                 [org.apache.lucene/lucene-analyzers "3.5.0"]]
  :main clj-thatfinger.server)