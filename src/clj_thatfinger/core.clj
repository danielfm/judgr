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
  "Returns the smoothing factor for the given class count."
  [cls-count]
  (if *smoothing-enabled*
    (* *smoothing-factor* cls-count)
    0))

(defn prob
  "Returns the weighted probability, if smoothing is enabled."
  [count-cat count-total cls-count]
  (let [count-cat (+ (or count-cat 0) (cat-factor))
        count-total (+ (or count-total 0) (total-factor cls-count))]
    (/ count-cat count-total)))

(defn prob-of-class
  "Returns the weighted probability of a message to be part of class cls."
  [cls]
  (let [cls-count (count *classes*)]
    (if *classes-unbiased*
      (prob 1 cls-count cls-count)
      (prob (count-messages cls) (count-messages) cls-count))))

(defn prob-of-word
  "Returns the probability of a word given the message is classified as cls."
  [word cls]
  (let [w (get-word word)]
    (prob (cls (:classes w)) (count-messages cls) (count-words))))

(defn posterior-prob-of-word
  "Returns the probability of cls given word is present."
  [cls word]
  (let [prior (* (prob-of-word word cls) (prob-of-class cls))
        total-prob (reduce + (map #(* (prob-of-word word %)
                                      (prob-of-class %)) *classes*))]
    (if (zero? total-prob)
      0
      (/ prior total-prob))))

(defn posterior-prob-of-message
  "Returns the probability that message is classified as class cls."
  [message cls]
  (let [words (stem message)]
    (reduce * (map #(posterior-prob-of-word cls %) words))))

(defn posterior-probs
  "Returns the probabilities of message for each possible class."
  [message]
  (zipmap *classes* (map #(posterior-prob-of-message message %) *classes*)))

(defn class-of-message
  "Returns the class with the highest probability for message that passes the
threshold validation."
  [message]
  (let [posterior-probs (reverse (sort-by val (posterior-probs message)))
        first-prob (first posterior-probs)
        second-prob (second posterior-probs)
        threshold ((key first-prob) *classes-threshold*)]
    (if (> (* (val second-prob) threshold) (val first-prob))
      *class-unknown*
      (key first-prob))))