(ns clj-thatfinger.core
  (:use [clj-thatfinger.db.default-db])
  (:use [clj-thatfinger.settings]))

(defn cat-factor
  "Returns the smoothing factor for a category."
  []
  (if *smoothing-enabled*
    *smoothing-factor*
    0))

(defn total-factor
  "Returns the smoothing factory for all categories."
  []
  (if *smoothing-enabled*
    (* *smoothing-factor* (count *classes*))
    0))

(defn prob
  "Returns the probability with smoothing, if it's enabled."
  [count-cat count-total]
  (/ (+ count-cat (cat-factor))
     (+ count-total (total-factor))))

(defn word-prob
  "Returns the probability of a word to be part of a category cat."
  [word cat]
  (let [w (get-word word)]
    (prob (cat (:categories w)) (:total w))))