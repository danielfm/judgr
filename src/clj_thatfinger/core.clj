(ns clj-thatfinger.core
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
    (* *smoothing-factor* *classes-count*)
    0))