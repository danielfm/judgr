(ns clj-thatfinger.probability
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