(ns clj-thatfinger.models.word
  (:use [clj-thatfinger.core])
  (:use [clj-thatfinger.db.default-db]))

(defn word-prob
  "Returns the probability of a word to be part of a category cat."
  [word cat]
  (let [w (get-word word)]
    (prob (cat (:categories w)) (:total w))))