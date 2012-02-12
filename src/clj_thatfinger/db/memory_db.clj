(ns clj-thatfinger.db.memory-db
  (:import [java.util Date])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(def ^:dynamic *messages* (atom {}))
(def ^:dynamic *words* (atom {}))

(defn module-name
  "Returns a name that describes this module."
  []
  "memory")

(defn get-word
  "Returns information about a word."
  [word]
  (@*words* word))

(defn count-messages
  "Returns the total number of messages of an optional class cls."
  ([]    (count @*messages*))
  ([cls] (count (filter #(= cls (:class %)) (vals @*messages*)))))

(defn count-words
  "Returns the total number of words."
  []
  (count @*words*))

(defn- update-word!
  "Updates the statistics of a word according to the class cls."
  [word cls]
  (let [w (get-word word)]
    (if (nil? w)
      (swap! *words* assoc word {:word word
                                 :total 1
                                 :classes {cls 1}})
      (let [cls-count (cls (:classes w))
            new-count (if (nil? cls-count) 1 (inc cls-count))]
        (swap! *words* assoc word (assoc-in (assoc w :total (inc (:total w)))
                                            [:classes cls] new-count))))))

(defn add-message!
  "Stores a message indicating its class."
  [message cls]
  (let [words (stem message)]
    (doall (map #(update-word! % cls) words))
    (swap! *messages* assoc message {:message message
                                     :words words
                                     :created-at (Date.)
                                     :class cls})))