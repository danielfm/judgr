(ns judgr.test.probability
  (:use [judgr.probability]
        [judgr.test.util]
        [clojure.test]))

(deftest calculating-inverse-chi-squared
  (is (close-to? 0.606 (inv-chi-sq 1 2)))
  (is (close-to? 0.082 (inv-chi-sq 5 2)))
  (is (close-to? 0.006 (inv-chi-sq 10 2))))

(deftest combining-probabilities-with-fishers-method
  (is (close-to? 0.02934 (fisher-prob '(0.03 0.1 0.3)))))