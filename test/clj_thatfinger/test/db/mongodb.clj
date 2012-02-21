(ns clj-thatfinger.test.db.mongodb
  (:use [clj-thatfinger.db.mongodb]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest add-message-fn
  (with-fixture test-mongo-db []
    (testing "adding message with invalid class"
      (is (thrown? IllegalArgumentException (add-message! "Uma mensagem" :some-class))))))

(deftest counting-messages
  (with-fixture test-mongo-db []
    (testing "counting all messages"
      (is (= 4 (count-messages))))

    (testing "counting messages of a class"
      (is (= 3 (count-messages :offensive))))))

(deftest counting-words
  (with-fixture test-mongo-db []
    (testing "counting all words"
      (is (= 11 (count-words))))))

(deftest get-word-fn
  (with-fixture test-mongo-db []
    (testing "information about a word"
      (let [word (get-word "diab")]
        (is (= "diab" (:word word)))))))

(deftest get-word-info
  (with-fixture test-mongo-db []
    (let [word (get-word "diab")]
      (is (= 3 (-> word :total)))
      (is (= '(:offensive :ok) (-> word :classes keys)))
      (is (= 1 (-> word :classes :ok)))
      (is (= 2 (-> word :classes :offensive))))))