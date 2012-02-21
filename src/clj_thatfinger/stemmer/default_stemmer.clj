(ns clj-thatfinger.stemmer.default-stemmer
  (:use [clj-thatfinger.settings]))

(defn- module
  "Returns the symbol that points to the configured stemmer module in settings."
  []
  (symbol (str "clj-thatfinger.stemmer." *stemmer-module*)))

(defn- module-fn
  "Resolves a symbol from the configured stemmer module."
  [sym]
  (apply require [(module)])
  (ns-resolve (module) sym))

(defn stemmer-module-name
  "Returns a name that describes this module."
  []
  (let [f (module-fn 'stemmer-module-name)]
    (f)))

(defn stem
  "Returns a set with the stemming output for string s."
  [s]
  (let [f (module-fn 'stem)]
    (f s)))