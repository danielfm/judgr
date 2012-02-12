(ns clj-thatfinger.db.memory-db
  (:import [java.util Date])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(def ^:dynamic *messages* (atom {}))
(def ^:dynamic *words* (atom {}))

(defn module-name
  ""
  []
  "memory")

(defn get-word
  ""
  [word]
  (@*words* word))

(defn count-messages
  ""
  ([]    (count @*messages*))
  ([cls] (count (filter #(= cls (:class %)) (vals @*messages*)))))

(defn count-words
  ""
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
  ""
  [message cls]
  (let [words (stem message)]
    (doall (map #(update-word! % cls) words))
    (swap! *messages* assoc message {:message message
                                     :words words
                                     :created-at (Date.)
                                     :class cls})))