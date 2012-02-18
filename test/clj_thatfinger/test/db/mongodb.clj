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
    (add-message! "Uma mensagem" :ok)
    (f)))

(use-fixtures :each test-database)

(deftest add-message-fn
  (testing "adding message with invalid class"
    (is (thrown? IllegalArgumentException (add-message! "Uma mensagem" :some-class)))))

(deftest counting-messages
  (add-message! "Outra mensagem" :offensive)

  (testing "counting all messages"
    (is (= 2 (count-messages))))

  (testing "counting messages of a class"
    (is (= 1 (count-messages :offensive)))))

(deftest counting-words
  (add-message! "Um texto" :ok)

  (testing "counting all words"
    (is (= 2 (count-words)))))

(deftest get-word-fn
  (testing "information about a word"
    (let [word (get-word "mensag")]
      (is (= "mensag" (:word word)))
      (is (= 1 (:total word)))
      (is (= '(:ok) (keys (:classes word))))
      (is (= 1 (:ok (:classes word)))))))