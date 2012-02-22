(ns clj-thatfinger.db.memory-db
  (:import [java.util Date])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(def ^:dynamic *items* (atom {}))
(def ^:dynamic *features* (atom {}))

(defn memory-module-name
  "Returns a name that describes this module."
  []
  "memory-db")

(defn get-items
  "Returns all items."
  []
  (vals @*items*))

(defn get-feature
  "Returns information about a feature."
  [feature]
  (@*features* feature))

(defn count-items
  "Returns the total number of items."
  []
  (count @*items*))

(defn count-items-of
  "Returns the number of items that belong to a class."
  [class]
  (count (filter #(= (:class %) class) (vals @*items*))))

(defn count-features
  "Returns the total number of features."
  []
  (count @*features*))

(defn- update-feature!
  "Updates the statistics of a feature according to the class."
  [feature class]
  (if (nil? (*classes* class))
    (throw (IllegalArgumentException. "Invalid class"))
    (let [f (get-feature feature)]
      (if-not (nil? (*classes* class))
        (if (nil? f)
          (swap! *features* assoc feature {:feature feature
                                           :total 1
                                           :classes {class 1}})
          (let [total-count (or (-> f :total) 0)
                class-count (or (-> f :classes class) 0)]
            (swap! *features* assoc feature
                   (assoc-in (assoc f :total (inc total-count))
                             [:classes class] (inc class-count)))))))))

(defn add-item!
  "Stores an item indicating its class."
  [item class]
  (let [features (stem item)]
    (doall (map #(update-feature! % class) features))
    (swap! *items* assoc item {:item item
                               :features features
                               :created-at (Date.)
                               :class class})))

(defmacro with-memory-db
  "Runs body using an empty in-memory database."
  [& body]
  `(binding [clj-thatfinger.settings/*db-module* 'memory-db
             *items* (atom {})
             *features* (atom {})]
     ~@body))