(ns clj-thatfinger.test.db.memory-db
  (:use [clj-thatfinger.db.memory-db]
        [clj-thatfinger.test.fixtures]
        [clj-thatfinger.test.utils]
        [clojure.test]))

(deftest adding-messages
  (with-fixture test-memory-db []
    (testing "adding message with invalid class"
      (is (thrown? IllegalArgumentException
                   (add-message! "Uma mensagem" :some-class))))))

(deftest counting-messages
  (with-fixture test-memory-db []
    (testing "counting all messages"
      (is (= 4 (count-messages))))

     (testing "counting messages of a class"
       (is (= 3 (count-messages :offensive))))))

 (deftest counting-words
   (with-fixture test-memory-db []
     (testing "counting all words"
       (is (= 11 (count-words))))))

(deftest get-word-fn
  (with-fixture test-memory-db []
    (testing "information about a word"
      (let [word (get-word "diab")]
        (is (= "diab" (:word word)))))))

(deftest get-word-info
  (with-fixture test-memory-db []
    (let [word (get-word "diab")]
      (is (= 3 (-> word :total)))
      (is (= '(:offensive :ok) (-> word :classes keys)))
      (is (= 1 (-> word :classes :ok)))
      (is (= 2 (-> word :classes :offensive))))))