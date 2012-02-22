(ns clj-thatfinger.test.cross-validation
  (:use [clj-thatfinger.cross-validation]
        [clj-thatfinger.db.default-db]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest partition-items-fn
  (with-fixture test-memory-db []
    (testing "valid partitions"
      (let [partitions (partition-items 1)]
        (is (= 1 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido."
                 "Vai pro inferno, diabo!"
                 "Sai de ré, capeta."
                 "Você é um diabo, mesmo.")
               (map :item (first partitions))))))

    (testing "partition into zero subsets"
      (let [partitions (partition-items 0)]
        (is (= 1 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido."
                 "Vai pro inferno, diabo!"
                 "Sai de ré, capeta."
                 "Você é um diabo, mesmo.")
               (map :item (first partitions))))))

    (testing "partition into too many subsets"
      (let [partitions (partition-items 5)]
        (is (= 2 (count partitions)))
        (is (= '("Sua filha é uma diaba, doido." "Vai pro inferno, diabo!")
               (map :item (first partitions))))
        (is (= '("Sai de ré, capeta." "Você é um diabo, mesmo.")
               (map :item (second partitions))))))))

(deftest remove-nth-fn
  (testing "remove the nth element from a collection"
    (is (= '(0 1 3 4) (remove-nth 2 (range 5)))))

  (testing "remove an element out of range"
    (is (= (range 5) (remove-nth 10 (range 5))))))

(deftest train-partition-fn
  (with-fixture test-memory-db []
    (testing "train another batch of items"
      (is (= 4 (count-items)))
      (train-partition! '({:item "Tudo bem?"  :class :ok}
                          {:item "Maravilha." :class :ok}))
      (is (= 6 (count-items))))))

(deftest train-all-partitions-but-fn
  (with-fixture test-memory-db []
    (testing "train all but the nth partition"
      (let [partitions '(({:item "Tudo bem?"  :class :ok}
                          {:item "Maravilha." :class :ok})
                         ({:item "Tudo ok?"   :class :ok}))]
        (train-all-partitions-but! 0 partitions)
        (is (= 5 (count-items)))))))

(deftest expected-predicted-count-fn
  (with-fixture threshold [{:ok {:threshold 1.2} :offensive {:threshold 2.5}}]
    (with-fixture test-memory-db []
      (testing "expected-predicted count map when prediction matches"
        (is (= {:offensive {:offensive 1}}
               (expected-predicted-count {:item "Lugar de diabo é no inferno." :class :offensive}))))

      (testing "expected-predicted count map when prediction doesn't match"
        (is (= {:ok {:offensive 1}}
               (expected-predicted-count {:item "Lugar de diabo é no inferno." :class :ok})))))))

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
      (testing "evaluate a trained model against new items"
        (let [msgs '({:item "Oi mesmo..." :class :ok} ;; predicted as unknown
                     {:item "Sou eu mesmo!" :class :unknown}
                     {:item "Vai seu diabo dos infernos" :class :offensive}
                     {:item "Filha do diabo!" :class :offensive}
                     {:item "Capeta." :class :offensive})]
          (is (= {:offensive {:offensive 3} :unknown {:unknown 1} :ok {:unknown 1}}
                 (eval-model msgs))))))))

(deftest true-positives-fn
  (with-fixture confusion-matrix []
    (testing "get the true positive count from a confusion matrix"
      (is (= 25 (true-positives :a conf-matrix)))
      (is (= 32 (true-positives :b conf-matrix)))
      (is (= 15 (true-positives :c conf-matrix)))
      (is (= 72 (true-positives conf-matrix))))))

(deftest false-positives-fn
  (with-fixture confusion-matrix []
    (testing "get false positive count from a confusion matrix"
      (is (= 4 (false-positives :a conf-matrix)))
      (is (= 5 (false-positives :b conf-matrix)))
      (is (= 6 (false-positives :c conf-matrix)))
      (is (= 15 (false-positives conf-matrix))))))

(deftest false-negatives-fn
  (with-fixture confusion-matrix []
    (testing "get false negative count from a confusion matrix"
      (is (= 7 (false-negatives :a conf-matrix)))
      (is (= 7 (false-negatives :b conf-matrix)))
      (is (= 1 (false-negatives :c conf-matrix)))
      (is (= 15 (false-negatives conf-matrix))))))

(deftest true-negatives-fn
  (with-fixture confusion-matrix []
    (testing "get true negatives count from a confusion matrix"
      (is (= 51 (true-negatives :a conf-matrix)))
      (is (= 43 (true-negatives :b conf-matrix)))
      (is (= 65 (true-negatives :c conf-matrix)))
      (is (= 159 (true-negatives conf-matrix))))))

(deftest precision-fn
  (with-fixture confusion-matrix []
    (testing "calculate the precision of a class from a confusion matrix"
      (is (float= 0.86206 (precision :a conf-matrix)))
      (is (float= 0.86486 (precision :b conf-matrix)))
      (is (float= 0.714285 (precision :c conf-matrix))))))

(deftest recall-fn
  (with-fixture confusion-matrix []
    (testing "calculate the recall of a class from a confusion matrix"
      (is (float= 0.78125 (recall :a conf-matrix)))
      (is (float= 0.82051 (recall :b conf-matrix)))
      (is (float= 0.9375 (recall :c conf-matrix))))

    (testing "sensitivity alias"
      (is (float= 0.78125 (sensitivity :a conf-matrix)))
      (is (float= 0.82051 (sensitivity :b conf-matrix)))
      (is (float= 0.9375 (sensitivity :c conf-matrix))))))

(deftest specificity-fn
  (with-fixture confusion-matrix []
    (testing "calculate the specificity of a class from a confusion matrix"
      (is (float= 0.92727 (specificity :a conf-matrix)))
      (is (float= 0.89583 (specificity :b conf-matrix)))
      (is (float= 0.91549 (specificity :c conf-matrix))))))

(deftest accuracy-fn
  (with-fixture confusion-matrix []
    (testing "calculate the accuracy from a confusion matrix"
      (is (float= 0.88505 (accuracy conf-matrix))))))

(deftest f1-score-fn
  (with-fixture confusion-matrix []
    (testing "calculate F1 score of a class from a confusion matrix"
      (is (float= 0.81967 (f1-score :a conf-matrix)))
      (is (float= 0.84210 (f1-score :b conf-matrix)))
      (is (float= 0.81081 (f1-score :c conf-matrix))))))