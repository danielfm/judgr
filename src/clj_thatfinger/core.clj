(ns clj-thatfinger.core
  (:use [clj-thatfinger.probability]
        [clj-thatfinger.db.default-db]
        [clj-thatfinger.stemmer.default-stemmer]
        [clj-thatfinger.settings]))

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
    (prob (-> w :classes cls) (count-messages cls) (count-words))))

(defn posterior-prob-of-word
  "Returns the probability of cls given word is present."
  [cls word]
  (let [prior (* (prob-of-word word cls) (prob-of-class cls))
        total-prob (reduce + (map #(* (prob-of-word word %)
                                      (prob-of-class %)) (keys *classes*)))]
    (if (zero? total-prob)
      0
      (float (/ prior total-prob)))))

(defn posterior-prob-of-message
  "Returns the probability that message is classified as class cls."
  [message cls]
  (let [words (stem message)]
    (fisher-prob (map #(posterior-prob-of-word cls %) words))))

(defn posterior-probs
  "Returns the probabilities of message for each possible class."
  [message]
  (let [classes (keys *classes*)]
    (zipmap classes (map #(posterior-prob-of-message message %) classes))))



(defn train!
  "Labels a message with the given class."
  [message cls]
  (add-message! message cls))

(defn classify
  "Returns the class with the highest probability for message that passes the
threshold validation."
  [message]
  (let [posterior-probs (reverse (sort-by val (posterior-probs message)))
        [first-prob second-prob & _]  posterior-probs]
    (if (or (not *threshold-enabled*)
            (<= (* (val second-prob)
                   (-> ((key first-prob) *classes*) :threshold))
                (val first-prob)))
      (key first-prob)
      *class-unknown*)))