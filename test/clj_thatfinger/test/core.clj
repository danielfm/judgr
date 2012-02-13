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

(deftest prob-of-class-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (testing "probability of :ok class"
        (is (= 2/5 (prob-of-class :ok))))

      (testing "probability of :offensive class"
        (is (= 3/5 (prob-of-class :offensive)))))))

(deftest prob-of-word-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (testing "probability of word being :ok"
        (is (= 2/3 (prob-of-word "diab" :ok))))

      (testing "probability of word being :offensive"
        (is (= 3/4 (prob-of-word "diab" :offensive)))))))

(deftest posterior-prob-of-word-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (testing "posterior probability of :ok given word"
        (is (= 16/43 (posterior-prob-of-word :ok "diab"))))

      (testing "posterior probability of :offensive given word"
        (is (= 27/43 (posterior-prob-of-word :offensive "diab")))))))

(deftest posterior-prob-of-message-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= 19683/237575
             (posterior-prob-of-message "Você adora o diabo, filha." :offensive))))))

(deftest posterior-probs-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= {:offensive 19683/237575
              :ok 8192/237575}
             (posterior-probs "Você adora o diabo, filha."))))))

(deftest class-of-message-fn
  (with-fixture smoothing []
    (with-fixture test-db []
      (is (= :offensive (class-of-message "Você adora o diabo, filha."))))))