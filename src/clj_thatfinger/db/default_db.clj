(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

(defn- module
  "Returns the symbol that points to the configured db module in settings."
  []
  (symbol (str "clj-thatfinger.db." *db-module*)))

(defn- module-fn
  "Resolves a symbol from the configured db module."
  [sym]
  (apply require [(module)])
  (ns-resolve (module) sym))

(defn memory-module-name
  "Returns a name that describes this module."
  []
  (let [f (module-fn 'memory-module-name)]
    (f)))

(defn add-message!
  "Stores a message indicating its class."
  [message cls]
  (let [f (module-fn 'add-message!)]
    (f message cls)))

(defn get-word
  "Returns information about a word."
  [word]
  (let [f (module-fn 'get-word)]
    (f word)))

(defn get-all-messages
  "Returns all messages."
  []
  (let [f (module-fn 'get-all-messages)]
    (f)))

(defn count-messages
  "Returns the total number of messages."
  ([]
     (let [f (module-fn 'count-messages)] (f)))
  ([cls]
     (let [f (module-fn 'count-messages)] (f cls))))

(defn count-words
  "Returns the total number of words."
  []
  (let [f (module-fn 'count-words)]
    (f)))