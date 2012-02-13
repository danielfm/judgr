(ns clj-thatfinger.core
  (:use [clj-thatfinger.db.default-db])
  (:use [clj-thatfinger.stemmer.default-stemmer])
  (:use [clj-thatfinger.settings]))

(defn cat-factor
  "Returns the smoothing factor for a category."
  []
  (if *smoothing-enabled*
    *smoothing-factor*
    0))

(defn total-factor
  "Returns the smoothing factory for all classes."
  []
  (if *smoothing-enabled*
    (* *smoothing-factor* (count *classes*))
    0))

(defn prob
  "Returns the probability with smoothing, if it's enabled."
  [count-cat count-total]
  (/ (+ (if (nil? count-cat) 0 count-cat) (cat-factor))
     (+ count-total (total-factor))))

(defn prob-of-class
  "Returns the probability of a message to be part of class cls."
  [cls]
  (if *classes-unbiased*
    (prob 1 (count *classes*))
    (prob (count-messages cls) (count-messages))))

(defn prob-of-word
  "Returns the probability of a word to be part of a class cls."
  [word cls]
  (let [w (get-word word)
        cls-prob (if-not (nil? w) (cls (:classes w)))]
    (prob cls-prob (count-messages cls))))

(defn posterior-prob-of-word
  "Returns the posterior probability that word appears in messages of class cls."
  [cls word]
  (let [prior (* (prob-of-word word cls) (prob-of-class cls))
        total-prob (reduce + (map #(* (prob-of-word word %)
                                      (prob-of-class %)) *classes*))]
    (/ prior total-prob)))

(defn posterior-prob-of-message
  "Returns the posterior probability that message is classified as class cls."
  [message cls]
  (let [words (stem message)]
    (reduce * (map #(posterior-prob-of-word cls %) words))))

(defn posterior-probs
  "Returns the posterior probabilities of message for each possible class."
  [message]
  (zipmap *classes* (map #(posterior-prob-of-message message %) *classes*)))

