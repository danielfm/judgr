(ns clj-thatfinger.db.memory-db
  (:import [java.util Date])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(def ^:dynamic *messages* (atom {}))
(def ^:dynamic *words* (atom {}))

(defmacro with-memory-db
  "Runs body using an empty in-memory database."
  [& body]
  `(binding [clj-thatfinger.settings/*db-module* 'memory-db
             *messages* (atom {})
             *words* (atom {})]
     ~@body))

(defn memory-module-name
  "Returns a name that describes this module."
  []
  "memory-db")

(defn get-all-messages
  "Returns all messages."
  []
  (vals @*messages*))

(defn get-word
  "Returns information about a word."
  [word]
  (@*words* word))

(defn messages-from
  "Returns all messages."
  []
  (filter #(contains? (set (keys %))) (vals @*messages*)))

(defn count-messages
  "Returns the total number of messages of an optional class cls."
  ([]
     (count @*messages*))
  ([cls]
     (count (filter #(= (:class %) cls) (vals @*messages*)))))

(defn count-words
  "Returns the total number of words."
  []
  (count @*words*))

(defn- update-word!
  "Updates the statistics of a word according to the class cls."
  [word cls]
  (if (nil? (*classes* cls))
    (throw (IllegalArgumentException. "Invalid class"))
    (let [w (get-word word)]
      (if-not (nil? (*classes* cls))
        (if (nil? w)
          (swap! *words* assoc word {:word word
                                     :total 1
                                     :classes {cls 1}})
          (let [total-count (or (-> w :total) 0)
                cls-count (or (-> w :classes cls) 0)]
            (swap! *words* assoc word
                   (assoc-in (assoc w :total (inc total-count))
                             [:classes cls] (inc cls-count)))))))))

(defn add-message!
  "Stores a message indicating its class."
  [message cls]
  (let [words (stem message)]
    (doall (map #(update-word! % cls) words))
    (swap! *messages* assoc message {:message message
                                     :words words
                                     :created-at (Date.)
                                     :class cls})))