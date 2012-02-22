(ns clj-thatfinger.test.cross-validation
  (:use [clj-thatfinger.cross-validation]
        [clj-thatfinger.db.default-db]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest partition-messages-fn
  (with-fixture test-memory-db []
    (testing "valid partitions"
      (let [partitions (partition-messages 1)]
        (is (= 1 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido."
                 "Vai pro inferno, diabo!"
                 "Sai de ré, capeta."
                 "Você é um diabo, mesmo.")
               (map :message (first partitions))))))

    (testing "partition into zero subsets"
      (let [partitions (partition-messages 0)]
        (is (= 1 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido."
                 "Vai pro inferno, diabo!"
                 "Sai de ré, capeta."
                 "Você é um diabo, mesmo.")
               (map :message (first partitions))))))

    (testing "partition into too many subsets"
      (let [partitions (partition-messages 5)]
        (is (= 2 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido." "Vai pro inferno, diabo!")
               (map :message (first partitions))))
        (is (= '("Sai de ré, capeta." "Você é um diabo, mesmo.")
               (map :message (second partitions))))))))

(deftest remove-nth-fn
  (testing "remove the nth element from a collection"
    (is (= '(0 1 3 4) (remove-nth 2 (range 5)))))

  (testing "remove an element out of range"
    (is (= (range 5) (remove-nth 10 (range 5))))))

(deftest train-partition-fn
  (with-fixture test-memory-db []
    (testing "train another batch of messages"
      (is (= 4 (count-messages)))
      (train-partition! '({:message "Tudo bem?"  :class :ok}
                          {:message "Maravilha." :class :ok}))
      (is (= 6 (count-messages))))))

(deftest train-all-partitions-but-fn
  (with-fixture test-memory-db []
    (testing "train all but the nth partition"
      (let [partitions '(({:message "Tudo bem?"  :class :ok}
                          {:message "Maravilha." :class :ok})
                         ({:message "Tudo ok?"   :class :ok}))]
        (train-all-partitions-but! 0 partitions)
        (is (= 5 (count-messages)))))))

(deftest expected-predicted-count-fn
  (with-fixture threshold [{:ok {:threshold 1.2} :offensive {:threshold 2.5}}]
    (with-fixture test-memory-db []
      (testing "expected-predicted count map when prediction matches"
        (is (= {:offensive {:offensive 1}}
               (expected-predicted-count {:message "Lugar de diabo é no inferno." :class :offensive}))))

      (testing "expected-predicted count map when prediction doesn't match"
        (is (= {:ok {:offensive 1}}
               (expected-predicted-count {:message "Lugar de diabo é no inferno." :class :ok})))))))

(deftest nested-dissoc-fn
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

(deftest add-results-fn
  (testing "combine expected-predicted count maps"
    (is (= {:a 1 :b 1}
           (add-results {:a 1} {:b 1})))
    (is (= {:a 1 :b 2}
           (add-results {:a 1 :b 1} {:b 1})))
    (is (= {:a {:b 1 :c 1}}
           (add-results {:a {:b 1}} {:a {:c 1}})))
    (is (= {:a {:b 2 :c 1} :d 1}
           (add-results {:a {:b 1}} {:a {:b 1 :c 1} :d 1})))))

(deftest eval-model-fn
  (with-fixture threshold [{:ok {:threshold 1.2} :offensive {:threshold 2.5}}]
    (with-fixture test-memory-db []
      (testing "evaluate a trained model against new messages"
        (let [msgs '({:message "Oi mesmo..." :class :ok} ;; predicted as unknown
                     {:message "Sou eu mesmo!" :class :unknown}
                     {:message "Vai seu diabo dos infernos" :class :offensive}
                     {:message "Filha do diabo!" :class :offensive}
                     {:message "Capeta." :class :offensive})]
          (is (= {:offensive {:offensive 3} :unknown {:unknown 1} :ok {:unknown 1}}
                 (eval-model msgs))))))))

(deftest true-positives-fn
  (with-fixture confusion-matrix []
    (testing "get the true positive count of a given class from a confusion matrix"
      (is (= 25 (true-positives :a conf-matrix))))))

(deftest false-positives-fn
  (with-fixture confusion-matrix []
    (testing "get false positives of a class from a confusion matrix"
      (is (= 4 (false-positives :a conf-matrix))))))

(deftest false-negatives-fn
  (with-fixture confusion-matrix []
    (testing "get false negatives of a class from a confusion matrix"
      (is (= 7 (false-negatives :a conf-matrix))))))

(deftest true-negatives-fn
  (with-fixture confusion-matrix []
    (testing "get true negatives of a class from a confusion matrix"
      (is (= 51 (true-negatives :a conf-matrix))))))

(deftest precision-fn
  (with-fixture confusion-matrix []
    (testing "calculate the precision of a class from a confusion matrix"
      (is (float= 0.82758 (precision conf-matrix))))))

(deftest recall-fn
  (with-fixture confusion-matrix []
    (testing "calculate the recall of a class from a confusion matrix"
      (is (float= 0.82758 (recall conf-matrix))))))

(deftest specificity-fn
  (with-fixture confusion-matrix []
    (testing "calculate the specificity of a class from a confusion matrix"
      (is (float= 0.91379 (specificity conf-matrix))))))

(deftest accuracy-fn
  (with-fixture confusion-matrix []
    (testing "calculate the accuracy from a confusion matrix"
      (is (float= 0.88505 (accuracy conf-matrix))))))

(deftest f1-score-fn
  (with-fixture confusion-matrix []
    (testing "calculate F1 score for a class from a confusion matrix"
      (is (float= 0.82758 (f1-score conf-matrix))))))