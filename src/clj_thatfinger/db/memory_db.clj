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

(defn messages-from
  "Returns all messages from a given training subset."
  [subset]
  (filter #(contains? (set (keys %)) subset) (vals @*messages*)))

(defn count-messages
  "Returns the total number of messages of an optional class cls in the given
training subset."
  ([subset]     (count (messages-from subset)))
  ([cls subset] (count (filter #(= (subset %) cls) (vals @*messages*)))))

(defn- words-from
  "Returns all words from a given training subset."
  [subset]
  (filter #(subset %) (vals @*words*)))

(defn count-words
  "Returns the total number of words in the given training subset."
  [subset]
  (count (words-from subset)))

(defn- update-word!
  "Updates the statistics of a word according to the class cls in the given
training subset."
  [word cls subset]
  (if (nil? (*classes* cls))
    (throw (IllegalArgumentException. "Invalid class"))
    (let [w (get-word word)]
      (if-not (nil? (*classes* cls))
        (if (nil? w)
          (swap! *words* assoc word {:word word
                                     subset {:total 1
                                             :classes {cls 1}}})
          (let [total-count (or (-> w subset :total) 0)
                cls-count (or (-> w subset :classes cls) 0)]
            (swap! *words* assoc word
                   (assoc-in (assoc-in w [subset :total] (inc total-count))
                             [subset :classes cls] (inc cls-count)))))))))

(defn add-message!
  "Stores a message indicating its class in the given training subset."
  [message cls subset]
  (let [words (stem message)]
    (doall (map #(update-word! % cls subset) words))
    (swap! *messages* assoc message {:message message
                                     :words words
                                     :created-at (Date.)
                                     subset cls})))