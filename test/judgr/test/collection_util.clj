(ns judgr.test.collection-util
  (:use [judgr.collection-util]
        [clojure.test]))

(deftest removing-the-nth-element-from-collection
  (is (= '(0 1 3 4) (remove-nth 2 (range 5))))

  (testing "an index out of range"
    (is (= (range 5) (remove-nth 10 (range 5))))))

(deftest dissoc-nested-keys-in-map
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

(deftest aggregating-result-maps
  (is (= {:a 1 :b 1}
         (aggregate-results {:a 1} {:b 1})))
  (is (= {:a 1 :b 2}
         (aggregate-results {:a 1 :b 1} {:b 1})))
  (is (= {:a {:b 1 :c 1}}
         (aggregate-results {:a {:b 1}} {:a {:c 1}})))
  (is (= {:a {:b 2 :c 1} :d 1}
         (aggregate-results {:a {:b 1}} {:a {:b 1 :c 1} :d 1}))))

(deftest applying-function-to-each-key-of-map
  (is (= {:a 2 :b 3 :c 4}
         (apply-to-each-key (fn [k m]
                              (inc (k m)))
                            {:a 1 :b 2 :c 3}))))