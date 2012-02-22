(ns clj-thatfinger.extractors.default-extractor
  (:use [clj-thatfinger.settings]))

(defn- module-fn
  "Resolves a symbol from the configured extractor module."
  [sym]
  (require *extractor-module*)
  (ns-resolve *extractor-module* sym))

(defn extractor-module-name
  "Returns a name that describes this module."
  []
  (let [f (module-fn 'extractor-module-name)]
    (f)))

(defn extract-features
  "Returns a set of features extracted from string s."
  [s]
  (let [f (module-fn 'extract-features)]
    (f s)))