(ns clj-thatfinger.core
  (:use [clj-thatfinger.db.default-db])
  (:use [clj-thatfinger.stemmer.default-stemmer])
  (:use [clj-thatfinger.settings]))

(defn cls-factor
  "Returns the smoothing factor for a class."
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

(defn inv-chi-sq
  "Returns the inverse chi squared with df degrees of freedom."
  [chi df]
  (let [m (/ chi 2.0)]
    (min
     (reduce +
             (reductions * (Math/exp (- m)) (for [i (range 1 (/ df 2))] (/ m i))))
     1.0)))

(defn fisher-prob
  "Combines a list of independent probabilities into one using Fisher's method."
  [probs]
  (inv-chi-sq (* -2 (Math/log (reduce * probs)))
              (* 2 (count probs))))

(defn prob
  "Returns the weighted probability, if smoothing is enabled."
  [count-cls count-total cls-count]
  (let [count-cls (+ (or count-cls 0) (cls-factor))
        count-total (+ (or count-total 0) (total-factor cls-count))]
    (float (/ count-cls count-total))))

(defn prob-of-class
  "Returns the weighted probability of a message to be part of class cls."
  [cls subset]
  (let [cls-count (count *classes*)]
    (if *classes-unbiased*
      (prob 1 cls-count cls-count)
      (prob (count-messages cls subset) (count-messages subset) cls-count))))

(defn prob-of-word
  "Returns the probability of a word given the message is classified as cls."
  [word cls subset]
  (let [w (get-word word)]
    (prob (-> w subset :classes cls) (count-messages cls subset) (count-words subset))))

(defn posterior-prob-of-word
  "Returns the probability of cls given word is present."
  [cls word subset]
  (let [prior (* (prob-of-word word cls subset) (prob-of-class cls subset))
        total-prob (reduce + (map #(* (prob-of-word word % subset)
                                      (prob-of-class % subset)) (keys *classes*)))]
    (if (zero? total-prob)
      0
      (float (/ prior total-prob)))))

(defn posterior-prob-of-message
  "Returns the probability that message is classified as class cls."
  [message cls subset]
  (let [words (stem message)]
    (fisher-prob (map #(posterior-prob-of-word cls % subset) words))))

(defn posterior-probs
  "Returns the probabilities of message for each possible class."
  [message subset]
  (let [classes (keys *classes*)]
    (zipmap classes (map #(posterior-prob-of-message message % subset) classes))))

(defn class-of-message
  "Returns the class with the highest probability for message that passes the
threshold validation."
  [message subset]
  (let [posterior-probs (reverse (sort-by val (posterior-probs message subset)))
        first-prob (first posterior-probs)
        second-prob (second posterior-probs)
        threshold (:threshold ((key first-prob) *classes*))]
    (if (> (* (val second-prob) threshold) (val first-prob))
      *class-unknown*
      (key first-prob))))