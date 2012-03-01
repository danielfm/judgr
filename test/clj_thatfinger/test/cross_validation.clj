(ns clj-thatfinger.test.cross-validation
  (:use [clj-thatfinger.cross-validation]
        [clj-thatfinger.test.util]
        [clojure.test]))

(deftest removing-the-nth-element-from-collection
  (is (= '(0 1 3 4) (remove-nth 2 (range 5))))

  (testing "an index out of range"
    (is (= (range 5) (remove-nth 10 (range 5))))))

(deftest dissoc-nested-keys-in-map
  (let [a-map {:a 1 :b 2 :c {:d 3}}]
    (testing "dissoc a key in the map's root"
      (is (= {:a 1 :c {:d 3}}
             (nested-dissoc a-map [:b]))))

    (testing "dissoc an existing nested key"
      (is (= {:a 1 :b 2 :c {}}
             (nested-dissoc a-map [:c :d]))))

    (testing "dissoc a non existing key"
      (is (= a-map (nested-dissoc a-map [:e])))
      (is (= a-map (nested-dissoc a-map [:c :e]))))))

(deftest aggregating-result-maps
  (is (= {:a 1 :b 1}
         (aggregate-results {:a 1} {:b 1})))
  (is (= {:a 1 :b 2}
         (aggregate-results {:a 1 :b 1} {:b 1})))
  (is (= {:a {:b 1 :c 1}}
         (aggregate-results {:a {:b 1}} {:a {:c 1}})))
  (is (= {:a {:b 2 :c 1} :d 1}
         (aggregate-results {:a {:b 1}} {:a {:b 1 :c 1} :d 1}))))

(def-fixture confusion-matrix []
  (let [conf-matrix {:a {:a 25 :b 5  :c 2}
                     :b {:a 3  :b 32 :c 4}
                     :c {:a 1  :b 0  :c 15}}]
    (test-body)))

(deftest summing-true-negatives-from-class
  (with-fixture confusion-matrix []
    (is (= 72 (true-positives conf-matrix)))
    (are [cls tot] (= tot (true-positives cls conf-matrix))
         :a 25
         :b 32
         :c 15)))

(deftest summing-false-positives-from-class
  (with-fixture confusion-matrix []
    (is (= 15 (false-positives conf-matrix)))
    (are [cls tot] (= tot (false-positives cls conf-matrix))
         :a 4
         :b 5
         :c 6)))

(deftest summing-false-negatives-from-class
  (with-fixture confusion-matrix []
    (is (= 15 (false-negatives conf-matrix)))
    (are [cls tot] (= tot (false-negatives cls conf-matrix))
         :a 7
         :b 7
         :c 1)))

(deftest summing-true-negatives-from-class
  (with-fixture confusion-matrix []
    (is (= 159 (true-negatives conf-matrix)))
    (are [cls tot] (= tot (true-negatives cls conf-matrix))
         :a 51
         :b 43
         :c 65)))

(deftest calculating-precision-of-class
  (with-fixture confusion-matrix []
    (are [cls p] (close-to? p (precision cls conf-matrix))
         :a 0.86206
         :b 0.86486
         :c 0.714285)))

(deftest calculating-recall-of-class
  (with-fixture confusion-matrix []
    (are [cls r] (close-to? r (recall cls conf-matrix))
         :a 0.78125
         :b 0.82051
         :c 0.9375)

    (testing "sensitivity alias function"
      (are [cls r] (close-to? r (sensitivity cls conf-matrix))
           :a 0.78125
           :b 0.82051
           :c 0.9375))))

(deftest calculating-specificity-of-class
  (with-fixture confusion-matrix []
    (are [cls s] (close-to? s (specificity cls conf-matrix))
         :a 0.92727
         :b 0.89583
         :c 0.91549)))

(deftest calculating-f1-score
  (with-fixture confusion-matrix []
    (are [cls f] (close-to? f (f1-score cls conf-matrix))
         :a 0.81967
         :b 0.84210
         :c 0.81081)))

(deftest calculating-accuracy
  (with-fixture confusion-matrix []
    (is (close-to? 0.88505 (accuracy conf-matrix)))))