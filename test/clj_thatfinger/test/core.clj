(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest prob-of-class-fn
  (with-fixture test-memory-db []
    (testing "with smoothing"
      (with-fixture smoothing [1]
        (is (float= 1/3 (prob-of-class :ok)))
        (is (float= 2/3 (prob-of-class :offensive)))))

    (testing "without smoothing"
      (with-fixture smoothing [0]
        (is (float= 1/4 (prob-of-class :ok)))
        (is (float= 3/4 (prob-of-class :offensive)))))

    (testing "unbiased probability"
      (with-fixture unbiased [true]
        (is (float= 1/2 (prob-of-class :ok)))
        (is (float= 1/2 (prob-of-class :offensive)))))))

(deftest prob-of-feature-fn
  (with-fixture test-memory-db []
    (testing "with smoothing"
      (with-fixture smoothing [1]
        (is (float= 1/6 (prob-of-feature "diab" :ok)))
        (is (float= 3/14 (prob-of-feature "diab" :offensive)))

        (testing "inexistent feature"
          (is (float= 1/12 (prob-of-feature "lombra" :ok))))))

    (testing "without smoothing"
      (with-fixture smoothing [0]
        (is (float= 1 (prob-of-feature "diab" :ok)))
        (is (float= 2/3 (prob-of-feature "diab" :offensive)))

        (testing "inexistent feature"
          (is (zero? (prob-of-feature "lombra" :offensive))))))))

(deftest posterior-prob-of-feature-fn
  (with-fixture test-memory-db []
    (testing "with smoothing"
      (with-fixture smoothing [1]
        (is (float= 7/25 (posterior-prob-of-feature :ok "diab")))
        (is (float= 18/25 (posterior-prob-of-feature :offensive "diab")))))

    (testing "without smoothing"
      (with-fixture smoothing [0]
        (is (float= 1/3 (posterior-prob-of-feature :ok "diab")))
        (is (float= 2/3 (posterior-prob-of-feature :offensive "diab")))))))

(deftest posterior-prob-of-item-fn
  (with-fixture test-memory-db []
    (with-fixture smoothing [1]
      (testing "probability of item being :offensive"
        (is (float= 4112702/4656612
                    (posterior-prob-of-item "Filha do diabo." :offensive)))))))

(deftest probs-fn
  (with-fixture test-memory-db []
    (with-fixture smoothing [1]
      (testing "probabilities of item for each possible class"
        (let [probs (probs "Filha do diabo.")]
          (is (float= 4112702/4656612 (:offensive probs)))
          (is (float= 11073190/46566128 (:ok probs))))))))

(deftest classify-items
  (with-fixture test-memory-db []
    (with-fixture smoothing [1]
      (testing "class with greatest probability"
        (is (= :offensive (classify "Filha do diabo."))))

      (testing "unknown item due to failed threshold validation"
        (with-fixture threshold [{:offensive {:threshold 50}
                                  :ok {:threshold 1}}]
          (is (= :unknown (classify "Filha do diabo.")))

          (testing "turning off threshold validation"
            (with-fixture without-threshold []
              (is (= :offensive (classify "Filha do diabo."))))))))))