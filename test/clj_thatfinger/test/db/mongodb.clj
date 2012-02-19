(ns clj-thatfinger.test.db.mongodb
  (:use [clj-thatfinger.db.mongodb])
  (:require [somnium.congomongo :as mongodb])
  (:use [clojure.test]))

(defn- remove-collections!
  "Removes all collections from MongoDB."
  []
  (mongodb/drop-coll! :words)
  (mongodb/drop-coll! :messages))

(defn test-database [f]
  (binding [clj-thatfinger.settings/*mongodb-database* "thatfinger-test"]
    (create-connection!)
    (remove-collections!)
    (add-message! "Uma mensagem" :ok :training)
    (f)))

(use-fixtures :each test-database)

(deftest add-message-fn
  (testing "adding message with invalid class"
    (is (thrown? IllegalArgumentException (add-message! "Uma mensagem" :some-class :training))))

  (testing "adding message in another subset"
    (add-message! "Uma mensagem" :ok :test)
    (is (= 1 (count-messages :training)))))

(deftest counting-messages
  (add-message! "Outra mensagem" :offensive :training)

  (testing "counting all messages"
    (is (= 2 (count-messages :training))))

  (testing "counting all messages in a subset that doesn't exists"
    (is (zero? (count-messages :test))))

  (testing "counting messages of a class"
    (is (= 1 (count-messages :offensive :training))))

  (testing "counting messages of a class in subset that doesn't exist"
    (is (zero? (count-messages :ok :test)))))

(deftest counting-words
  (add-message! "Um texto" :ok :training)

  (testing "counting all words"
    (is (= 2 (count-words :training)))))

(deftest get-word-fn
  (testing "information about a word"
    (let [word (get-word "mensag")]
      (is (= "mensag" (:word word))))))

(deftest counters-per-subset
  (add-message! "Sua mensagem" :ok :test)
  (add-message! "Outra mensagem" :ok :test)
  (add-message! "Mensagem do inferno" :offensive :test)
  (let [word (get-word "mensag")]

    (testing "training subset"
      (is (= 1 (-> word :training :total)))
      (is (= '(:ok) (-> word :training :classes keys)))
      (is (= 1 (-> word :training :classes :ok)))
      (is (nil? (-> word :training :classes :offensive))))

    (testing "test subset"
      (is (= 3 (-> word :test :total)))
      (is (= '(:offensive :ok) (-> word :test :classes keys)))
      (is (= 2 (-> word :test :classes :ok)))
      (is (= 1 (-> word :test :classes :offensive))))))