(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core])
  (:use [clj-thatfinger.db.default-db])
  (:use [clojure.test])
  (:use [clj-thatfinger.test.fixtures]))

;; Fixtures

(def-fixture smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* true
            clj-thatfinger.settings/*smoothing-factor* 1
            clj-thatfinger.settings/*classes* '(:ok :offensive)]
    (test-body)))

(def-fixture no-smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* false]
    (test-body)))

(def-fixture test-db []
  (binding [clj-thatfinger.db.memory-db/*words* (atom {})
            clj-thatfinger.db.memory-db/*messages* (atom {})]
    (let [messages [["Você é um diabo, mesmo." :ok]
                    ["Vai pro inferno, diabo!" :offensive]
                    ["Sua filha é uma diaba, doido." :offensive]]]
      (doall (map #(apply add-message! %) messages)))
    (test-body)))

;; Tests

(deftest smoothing-factor-enabled
  (with-fixture smoothing []
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
    (with-fixture smoothing []
      (is (= 4/102 (prob 3 100)))
      (is (= 1/102 (prob 0 100)))
      (is (= 1/102 (prob nil 100)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (= 3/100 (prob 3 100)))
      (is (zero? (prob 0 100)))
      (is (zero? (prob nil 100))))))

(def class-probability
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= 2/5 (class-prob :ok)))
      (is (= 3/5 (class-prob :offensive))))))

(deftest word-probability
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= 2/3 (word-prob "diab" :ok)))
      (is (= 3/4 (word-prob "diab" :offensive))))))

(deftest word-class-probability
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= 16/43 (word-class-prob :ok "diab")))
      (is (= 27/43 (word-class-prob :offensive "diab"))))))

(deftest message-class-probability
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= 2187/18275
             (message-class-prob "Você adora o diabo." :offensive))))))

(deftest message-probabilities
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= {:offensive 19683/237575
              :ok 8192/237575}
             (message-probs "Você adora o diabo, filha."))))))