(ns judgr.probability)

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