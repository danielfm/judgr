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
                    ["Sai de ré, capeta." :offensive]
                    ["Vai pro inferno, diabo!" :offensive]
                    ["Sua filha é uma diaba, doido." :offensive]]]
      (doall (map #(apply add-message! %) messages)))
    (test-body)))

;; Tests

(deftest cat-factor-fn
  (testing "with smoothing"
    (with-fixture smoothing []
      (is (= 1 (cat-factor)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (zero? (cat-factor))))))

(deftest total-factor-fn
  (testing "with smoothing"
    (with-fixture smoothing []
      (is (= 2 (total-factor 2)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (zero? (total-factor 2))))))

(deftest prob-fn
  (testing "with smoothing"
    (with-fixture smoothing []
      (is (= 4/102 (prob 3 100 2)))
      (is (= 1/102 (prob 0 100 2)))
      (is (= 1/102 (prob nil 100 2)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (= 3/100 (prob 3 100 2)))
      (is (zero? (prob 0 100 2)))
      (is (zero? (prob nil 100 2))))))

(deftest prob-of-class-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (= 1/3 (prob-of-class :ok)))
        (is (= 2/3 (prob-of-class :offensive)))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (= 1/4 (prob-of-class :ok)))
        (is (= 3/4 (prob-of-class :offensive)))))))

(deftest prob-of-word-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (= 1/6 (prob-of-word "diab" :ok)))
        (is (= 3/14 (prob-of-word "diab" :offensive)))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (= 1 (prob-of-word "diab" :ok)))
        (is (= 2/3 (prob-of-word "diab" :offensive)))))))

(deftest posterior-prob-of-word-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (= 7/25 (posterior-prob-of-word :ok "diab")))
        (is (= 18/25 (posterior-prob-of-word :offensive "diab")))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (= 1/3 (posterior-prob-of-word :ok "diab")))
        (is (= 2/3 (posterior-prob-of-word :offensive "diab")))))))

(deftest posterior-prob-of-message-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "probability of message being :offensive"
        (is (= 31104/191425
               (posterior-prob-of-message "Você adora o diabo, filha." :offensive)))))))

(deftest posterior-probs-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "probabilities of message for each possible class"
        (is (= {:offensive 31104/191425 :ok 2401/191425}
               (posterior-probs "Você adora o diabo, filha.")))))))

(deftest class-of-message-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "class with greatest probability without using a threshold"
        (is (= :offensive (class-of-message "Você adora o diabo, filha.")))))))