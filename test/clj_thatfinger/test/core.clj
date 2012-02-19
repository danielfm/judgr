(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core]
        [clj-thatfinger.db.default-db]
        [clojure.test]
        [clj-thatfinger.test.utils]
        [clj-thatfinger.test.fixtures]))

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

(deftest classify-messages
  (with-fixture test-db []
    (with-fixture smoothing []
      (testing "class with greatest probability"
        (is (= :offensive (classify "Filha do diabo."))))

      (testing "unknown message due to failed threshold validation"
        (with-fixture threshold [{:offensive {:threshold 50}
                                  :ok {:threshold 1}}]
          (is (= :unknown (classify "Filha do diabo.")))

          (testing "turning off threshold validation"
            (with-fixture without-threshold []
              (is (= :offensive (classify "Filha do diabo."))))))))))