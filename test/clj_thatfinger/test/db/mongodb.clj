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
    (add-message! "Uma mensagem")
    (f)))

(use-fixtures :each test-database)

(deftest counting-messages
  (add-message! "Outra mensagem" true)

  (testing "counting all messages"
    (is (= 2 (count-messages))))

  (testing "counting offsensive messages"
    (is (= 1 (count-offensive-messages)))))

(deftest get-word-fn
  (testing "information about a word"
    (let [word (get-word "mensag")]
      (is (= "mensag" (:word word)))
      (is (= 1 (:total word)))
      (is (zero? (:total-offensive word))))))