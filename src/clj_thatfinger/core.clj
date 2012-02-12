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

(defn class-prob
  "Returns the probability of a message to be part of class cls."
  [cls]
  (if *classes-unbiased*
    (prob 1 (count *classes*))
    (prob (count-messages cls) (count-messages))))

(defn word-prob
  "Returns the probability of a word to be part of a class cls."
  [word cls]
  (let [w (get-word word)
        cls-prob (if-not (nil? w) (cls (:classes w)))]
    (prob cls-prob (count-messages cls))))

(defn word-class-prob
  "Returns the conditional probability that word appears in messages of class cls."
  [cls word]
  (let [prior (* (word-prob word cls) (class-prob cls))
        total-prob (reduce + (map #(* (word-prob word %)
                                      (class-prob %)) *classes*))]
    (/ prior total-prob)))