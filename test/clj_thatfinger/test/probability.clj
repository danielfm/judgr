(ns clj-thatfinger.test.probability
  (:use [clj-thatfinger.probability]
        [clojure.test]
        [clj-thatfinger.test.utils]
        [clj-thatfinger.test.fixtures]))

(deftest total-factor-fn
  (testing "with smoothing"
    (with-fixture smoothing [1]
      (is (= 2 (total-factor 2)))))

  (testing "without smoothing"
    (with-fixture smoothing [0]
      (is (zero? (total-factor 2))))))

(deftest prob-fn
  (testing "with smoothing"
    (with-fixture smoothing [1]
      (is (float= 4/102 (prob 3 100 2)))
      (is (float= 1/102 (prob 0 100 2)))
      (is (float= 1/102 (prob nil 100 2)))))

  (testing "without smoothing"
    (with-fixture smoothing [0]
      (is (float= 3/100 (prob 3 100 2)))
      (is (zero? (prob 0 100 2)))
      (is (zero? (prob nil 100 2))))))

(deftest inv-chi-sq-fn
  (testing "lower distribution of inverse chi squared distribution"
    (is (float= 0.606 (inv-chi-sq 1 2)))
    (is (float= 0.082 (inv-chi-sq 5 2)))
    (is (float= 0.006 (inv-chi-sq 10 2)))))

(deftest fisher-prob-fn
  (testing "combining probabilities using Fisher's method"
    (let [probs '(0.03 0.1 0.3)]
      (is (float= 0.02934 (fisher-prob probs))))))