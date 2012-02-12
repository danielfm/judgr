(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core])
  (:use [clj-thatfinger.db.default-db])
  (:use [clojure.test])
  (:use [clj-thatfinger.test.fixtures]))

;; Fixtures

(def-fixture smoothing [factor classes]
  (binding [clj-thatfinger.settings/*smoothing-enabled* true
            clj-thatfinger.settings/*smoothing-factor* factor
            clj-thatfinger.settings/*classes* classes]
    (test-body)))

(def-fixture no-smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* false]
    (test-body)))

(def-fixture test-db [& messages]
  (binding [clj-thatfinger.db.memory-db/*words* (atom {})
            clj-thatfinger.db.memory-db/*messages* (atom {})]
    (doall (map #(apply add-message! %) messages))
    (test-body)))

;; Tests

(deftest smoothing-factor-enabled
  (with-fixture smoothing [1 '(:ok :offensive)]
    (testing "smoothing factor for a category"
      (is (= 1 (cat-factor))))

    (testing "smoothing factor for all categories"
      (is (= 2 (total-factor))))))

(deftest smoothing-factor-disabled
  (with-fixture no-smoothing []
    (testing "smoothing factor for a category"
      (is (zero? (cat-factor))))

    (testing "smoothing factor for all categories"
      (is (zero? (total-factor))))))

(deftest probability-calculation
  (testing "with smoothing"
    (with-fixture smoothing [1 '(:ok :offensive)]
      (is (= 4/102 (prob 3 100)))
      (is (= 1/102 (prob 0 100)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (= 3/100 (prob 3 100)))
      (is (zero? (prob 0 100))))))

(def class-probability
  (with-fixture smoothing [1 '(:ok :offensive)]
    (with-fixture test-db [["Você é um diabo, mesmo." :ok]
                           ["Vai pro inferno, diabo!" :offensive]
                           ["Sua filha é uma diaba, doido." :offensive]]
      (is (= 2/5 (class-prob :ok)))
      (is (= 3/5 (class-prob :offensive))))))

(deftest word-probability
  (with-fixture smoothing [1 '(:ok :offensive)]
    (with-fixture test-db [["Você é um diabo, mesmo." :ok]
                           ["Vai pro inferno, diabo!" :offensive]
                           ["Sua filha é uma diaba, doido." :offensive]]
      (is (= 2/3 (word-prob "diab" :ok)))
      (is (= 3/4 (word-prob "diab" :offensive))))))