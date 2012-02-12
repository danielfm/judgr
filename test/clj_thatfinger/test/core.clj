(ns clj-thatfinger.test.core
  (:use [clj-thatfinger.core])
  (:use [clojure.test])
  (:use [clj-thatfinger.test.fixtures]))

(def-fixture three-class-smoothing [factor classes-count]
  (binding [clj-thatfinger.settings/*smoothing-enabled* true
            clj-thatfinger.settings/*smoothing-factor* factor
            clj-thatfinger.settings/*classes-count* classes-count]
    (test-body)))

(def-fixture no-smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* false]
    (test-body)))

(deftest smoothing-enabled
  (with-fixture three-class-smoothing [1 3]
    (testing "smoothing factor for a category"
      (is (= 1 (cat-factor))))

    (testing "smoothing factor for all categories"
      (is (= 3 (total-factor))))))

(deftest smoothing-disabled
  (with-fixture no-smoothing []
    (testing "smoothing factor for a category"
      (is (zero? (cat-factor))))

    (testing "smoothing factor for all categories"
      (is (zero? (total-factor))))))