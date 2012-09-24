(ns judgr.test.cross-validation
  (:use [judgr.cross-validation]
        [judgr.core]
        [judgr.test.util]
        [judgr.settings]
        [clojure.test]))

(def new-settings
  (update-settings settings
                   [:database :type] :memory
                   [:classifier :type] :default
                   [:extractor :type] :brazilian-text))

(def-fixture empty-db []
  (let [classifier (classifier-from new-settings)]
    (test-body)))

(def-fixture basic-db []
  (let [classifier (classifier-from new-settings)]
    (doall (map (fn [[item class]] (.train! classifier item class))
                '(["Você é um diabo, mesmo." :positive]
                  ["Sai de ré, capeta." :negative]
                  ["Vai pro inferno, diabo!" :negative]
                  ["Sua filha é uma diaba, doido." :negative])))
    (test-body)))

(def-fixture thresholds [thresholds]
  (let [new-settings (update-settings new-settings
                                      [:classifier :default :threshold?] true
                                      [:classifier :default :thresholds] thresholds)]
    (test-body)))

(def-fixture confusion-matrix []
  (let [conf-matrix {:a {:a 25 :b 5  :c 2}
                     :b {:a 3  :b 32 :c 4}
                     :c {:a 1  :b 0  :c 15}}]
    (test-body)))

(def-fixture incomplete-confusion-matrix []
  (let [conf-matrix {:a {:b 10 :d 2}
                     :b {:b 7}
                     :c {:a 6 :b 2}}]
    (test-body)))

(deftest summing-true-negatives-from-class
  (with-fixture confusion-matrix []
    (is (= 72 (true-positives conf-matrix)))
    (are [cls tot] (= tot (true-positives cls conf-matrix))
         :a 25
         :b 32
         :c 15))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (is (zero? (true-positives conf-matrix)))
      (are [cls tot] (= tot (true-positives cls conf-matrix))
           :a 0
           :b 0
           :c 0))))

(deftest summing-false-positives-from-class
  (with-fixture confusion-matrix []
    (is (= 15 (false-positives conf-matrix)))
    (are [cls tot] (= tot (false-positives cls conf-matrix))
         :a 4
         :b 5
         :c 6))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (is (= 18 (false-positives conf-matrix)))
      (are [cls tot] (= tot (false-positives cls conf-matrix))
           :a 6
           :b 12
           :c 0))))

(deftest summing-false-negatives-from-class
  (with-fixture confusion-matrix []
        (are [cls tot] (= tot (false-negatives cls conf-matrix))
         :a 7
         :b 7
         :c 1))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
            (are [cls tot] (= tot (false-negatives cls conf-matrix))
           :a 10
           :b 0
           :c 8))))

(deftest summing-true-negatives-from-class
  (with-fixture confusion-matrix []
    (is (= 159 (true-negatives conf-matrix)))
    (are [cls tot] (= tot (true-negatives cls conf-matrix))
         :a 51
         :b 43
         :c 65))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (is (= 36 (true-negatives conf-matrix)))
      (are [cls tot] (= tot (true-negatives cls conf-matrix))
           :a 9
           :b 8
           :c 19))))

(deftest calculating-precision-of-class
  (with-fixture confusion-matrix []
    (are [cls p] (close-to? p (precision cls conf-matrix))
         :a 25/29
         :b 32/37
         :c 5/7))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (are [cls p] (close-to? p (precision cls conf-matrix))
           :a 0
           :b 7/19
           :c 0))))

(deftest calculating-recall-of-class
  (with-fixture confusion-matrix []
    (are [cls r] (close-to? r (recall cls conf-matrix))
         :a 25/32
         :b 32/39
         :c 15/16)

    (testing "sensitivity alias function"
      (are [cls r] (close-to? r (sensitivity cls conf-matrix))
           :a 25/32
           :b 32/39
           :c 15/16)))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (are [cls r] (close-to? r (recall cls conf-matrix))
           :a 0
           :b 1
           :c 0))))

(deftest calculating-specificity-of-class
  (with-fixture confusion-matrix []
    (are [cls s] (close-to? s (specificity cls conf-matrix))
         :a 51/55
         :b 43/48
         :c 65/71))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
      (are [cls s] (close-to? s (specificity cls conf-matrix))
           :a 3/5
           :b 2/5
           :c 1))))

(deftest calculating-f1-score
  (with-fixture confusion-matrix []
    (are [cls f] (close-to? f (f1-score cls conf-matrix))
         :a 50/61
         :b 16/19
         :c 30/37))

  (testing "incomplete confusion matrix"
    (with-fixture incomplete-confusion-matrix []
          (are [cls f] (close-to? f (f1-score cls conf-matrix))
               :a 0
               :b 7/13
               :c 0))))

(deftest calculating-accuracy
  (with-fixture confusion-matrix []
    (is (close-to? 77/87 (accuracy conf-matrix)))))

(deftest partitioning-items
  (with-fixture basic-db []
    (testing "valid partitions"
      (let [partitions (partition-items 1 (.db classifier))]
        (is (= 1 (count partitions)))
        (is (= '("Você é um diabo, mesmo."
                 "Sai de ré, capeta."
                 "Vai pro inferno, diabo!"
                 "Sua filha é uma diaba, doido.")
               (map :item (first partitions))))))

    (testing "partition into zero subsets"
      (let [partitions (partition-items 0 (.db classifier))]
        (is (= 1 (count partitions)))
        (is (= '("Você é um diabo, mesmo."
                 "Sai de ré, capeta."
                 "Vai pro inferno, diabo!"
                 "Sua filha é uma diaba, doido.")
               (map :item (first partitions))))))

    (testing "partition into too many subsets"
      (let [partitions (partition-items 5 (.db classifier))]
        (is (= 2 (count partitions)))
        (is (= '("Você é um diabo, mesmo." "Sai de ré, capeta.")
               (map :item (first partitions))))
        (is (= '("Vai pro inferno, diabo!" "Sua filha é uma diaba, doido.")
               (map :item (second partitions))))))))

(deftest training-all-partitions-except-nth
  (with-fixture basic-db []
    (let [partitions '(({:item "Tudo bem?"  :class :positive}
                        {:item "Maravilha." :class :positive})
                       ({:item "Tudo ok?"   :class :positive}))]
      (train-all-partitions-but! 0 partitions classifier)
      (is (= 5 (.count-items (.db classifier)))))))

(deftest counting-expected-vs-predicted
  (with-fixture thresholds [{:negative 2.5 :positive 1.2}]
    (with-fixture basic-db []
      (testing "expected-predicted count map when prediction matches"
        (is (= {:negative {:negative 1}}
               (expected-predicted-count {:item "Lugar de diabo é no inferno." :class :negative}
                                         classifier))))

      (testing "expected-predicted count map when prediction doesn't match"
        (is (= {:positive {:negative 1}}
               (expected-predicted-count {:item "Lugar de diabo é no inferno." :class :positive}
                                         classifier)))))))

(deftest evaluating-a-model
  (with-fixture thresholds [{:negative 2.5 :positive 1.2}]
    (with-fixture basic-db []
      (let [items '({:item "Oi mesmo..." :class :positive} ;; predicted as unknown
                    {:item "Sou eu mesmo!" :class :unknown}
                    {:item "Vai seu diabo dos infernos" :class :negative}
                    {:item "Filha do diabo!" :class :negative}
                    {:item "Capeta." :class :negative})]
        (is (= {:negative {:negative 3} :unknown {:unknown 1} :positive {:unknown 1}}
               (eval-model items classifier)))))))