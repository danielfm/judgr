(ns clj-thatfinger.test.db.mongodb
  (:use [clj-thatfinger.db.mongodb]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest add-item-fn
  (with-fixture test-mongo-db []
    (testing "adding item with invalid class"
      (is (thrown? IllegalArgumentException (add-item! "Uma mensagem" :some-class))))))

(deftest counting-items
  (with-fixture test-mongo-db []
    (testing "counting all items"
      (is (= 4 (count-items))))

    (testing "counting items of a class"
      (is (= 3 (count-items-of :offensive))))))

(deftest counting-features
  (with-fixture test-mongo-db []
    (testing "counting all features"
      (is (= 11 (count-features))))))

(deftest get-feature-fn
  (with-fixture test-mongo-db []
    (testing "information about a feature"
      (let [feature (get-feature "diab")]
        (is (= "diab" (:feature feature)))))))

(deftest get-feature-info
  (with-fixture test-mongo-db []
    (let [feature (get-feature "diab")]
      (is (= 3 (-> feature :total)))
      (is (= '(:offensive :ok) (-> feature :classes keys)))
      (is (= 1 (-> feature :classes :ok)))
      (is (= 2 (-> feature :classes :offensive))))))