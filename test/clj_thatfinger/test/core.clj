(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core])
  (:use [clj-thatfinger.db.default-db])
  (:use [clojure.test])
  (:use [clj-thatfinger.test.utils]))

;; Fixtures

(def-fixture smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* true
            clj-thatfinger.settings/*smoothing-factor* 1]
    (test-body)))

(def-fixture no-smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* false]
    (test-body)))

(def-fixture threshold [classes]
  (binding [clj-thatfinger.settings/*class-unknown* :unknown
            clj-thatfinger.settings/*classes* classes]
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

(deftest cls-factor-fn
  (testing "with smoothing"
    (with-fixture smoothing []
      (is (= 1 (cls-factor)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
      (is (zero? (cls-factor))))))

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
      (is (float= 4/102 (prob 3 100 2)))
      (is (float= 1/102 (prob 0 100 2)))
      (is (float= 1/102 (prob nil 100 2)))))

  (testing "without smoothing"
    (with-fixture no-smoothing []
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

(deftest prob-of-class-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (float= 1/3 (prob-of-class :ok)))
        (is (float= 2/3 (prob-of-class :offensive)))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (float= 1/4 (prob-of-class :ok)))
        (is (float= 3/4 (prob-of-class :offensive)))))))

(deftest prob-of-word-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (float= 1/6 (prob-of-word "diab" :ok)))
        (is (float= 3/14 (prob-of-word "diab" :offensive)))

        (testing "inexistent word"
          (is (float= 1/12 (prob-of-word "lombra" :ok))))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (float= 1 (prob-of-word "diab" :ok)))
        (is (float= 2/3 (prob-of-word "diab" :offensive)))

        (testing "inexistent word"
          (is (zero? (prob-of-word "lombra" :offensive))))))))

(deftest posterior-prob-of-word-fn
  (with-fixture test-db []
    (testing "with smoothing"
      (with-fixture smoothing []
        (is (float= 7/25 (posterior-prob-of-word :ok "diab")))
        (is (float= 18/25 (posterior-prob-of-word :offensive "diab")))))

    (testing "without smoothing"
      (with-fixture no-smoothing []
        (is (float= 1/3 (posterior-prob-of-word :ok "diab")))
        (is (float= 2/3 (posterior-prob-of-word :offensive "diab")))))))

(deftest posterior-prob-of-message-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "probability of message being :offensive"
        (is (float= 4112702/4656612
                    (posterior-prob-of-message "Filha do diabo." :offensive)))))))

(deftest posterior-probs-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "probabilities of message for each possible class"
        (let [probs (posterior-probs "Filha do diabo.")]
          (is (float= 4112702/4656612 (:offensive probs)))
          (is (float= 11073190/46566128 (:ok probs))))))))

(deftest class-of-message-fn
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "class with greatest probability"
        (is (= :offensive (class-of-message "Filha do diabo."))))

      (testing "unknown message due to failed threshold validation"
        (with-fixture threshold [{:offensive {:threshold 50}
                                  :ok {:threshold 1}}]
          (is (= :unknown (class-of-message "Filha do diabo."))))))))