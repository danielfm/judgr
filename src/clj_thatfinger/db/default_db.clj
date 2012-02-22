(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

(defn- module-fn
  "Resolves a symbol from the configured db module."
  [sym]
  (require *db-module*)
  (ns-resolve *db-module* sym))

(defn memory-module-name
  "Returns a name that describes this module."
  []
  (let [f (module-fn 'memory-module-name)]
    (f)))

(defn add-item!
  "Stores an item indicating its class."
  [item class]
  (let [f (module-fn 'add-item!)]
    (f item class)))

(defn get-feature
  "Returns information about a feature."
  [feature]
  (let [f (module-fn 'get-feature)]
    (f feature)))

(defn get-items
  "Returns all items."
  []
  (let [f (module-fn 'get-items)]
    (f)))

(defn count-items
  "Returns the total number of items."
  []
  (let [f (module-fn 'count-items)]
    (f)))

(defn count-items-of
  "Returns the number of items that belong to a class."
  [class]
  (let [f (module-fn 'count-items-of)]
    (f class)))

(defn count-features
  "Returns the total number of features."
  []
  (let [f (module-fn 'count-features)]
    (f)))