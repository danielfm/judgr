(ns clj-thatfinger.test.db.memory-db
  (:use [clj-thatfinger.db.memory-db]
        [clj-thatfinger.db.factory]
        [clj-thatfinger.test.util]
        [clj-thatfinger.test.settings]
        [clojure.test]))

(def-fixture empty-db []
  (let [db (make-memory-db settings)]
    (test-body)))

(def-fixture basic-db []
  (let [db (make-memory-db settings)]
    (.add-item! db "Some message" :ok)
    (.add-item! db "Another message" :ok)
    (.add-feature! db "Some message" "message" :ok)
    (.add-feature! db "Another message" "message" :ok)
    (.add-feature! db "Another message" "another" :ok)
    (test-body)))

(deftest adding-items
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-item! db "Some message" :ok)]
        (is (= "Some message" (:item data)))
        (is (= :ok (:class data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-item! db "Uma mensagem" :some-class))))))

(deftest adding-features
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-feature! db "Some message" "message" :ok)]
        (is (= "message" (:feature data)))
        (is (= '(:ok) (-> data :classes keys)))
        (is (= 1 (-> data :classes :ok)))
        (is (= 1 (:total data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-feature! db "Uma mensagem" "message" :some-class))))))

(deftest updating-features
  (with-fixture basic-db []
    (let [data (.add-feature! db "Subliminar message" "message" :offensive)]
      (is (= "message" (:feature data)))
      (is (= '(:offensive :ok) (-> data :classes keys)))
      (is (= 2 (-> data :classes :ok)))
      (is (= 1 (-> data :classes :offensive)))
      (is (= 3 (:total data))))))

(deftest counting-features
  (with-fixture basic-db []
    (is (= 2 (.count-features db))))

  (testing "when there's no features"
    (with-fixture empty-db []
      (is (zero? (.count-features db))))))

(deftest getting-feature
  (with-fixture basic-db []
    (let [data (.get-feature db "message")]
      (is (= "message" (:feature data)))
      (is (= '(:ok) (-> data :classes keys)))
      (is (= 2 (-> data :classes :ok)))
      (is (= 2 (:total data))))

    (testing "when feature doesn't exist"
      (is (nil? (.get-feature db "void"))))))

(deftest getting-items
  (with-fixture basic-db []
    (is (= '("Some message"
             "Another message")
           (map :item (.get-items db))))

    (testing "when there's no items"
      (with-fixture empty-db []
        (is (= '() (.get-items db)))))))

(deftest counting-items
  (with-fixture basic-db []
    (is (= 2 (.count-items db))))

  (testing "when there's no items"
    (with-fixture empty-db []
      (is (zero? (.count-items db))))))

(deftest counting-items-of-class
  (with-fixture basic-db []
    (is (= 2 (.count-items-of-class db :ok)))

    (testing "when there's no items in class"
      (is (zero? (.count-items-of-class db :offensive))))))