(ns clj-thatfinger.test.db.memory-db
  (:use clj-thatfinger.db.memory-db)
  (:use clojure.test))

(defn test-database [f]
  (binding [clj-thatfinger.db.memory-db/*words* (atom {})
            clj-thatfinger.db.memory-db/*messages* (atom {})]
    (add-message! "Uma mensagem" :ok :training)
    (f)))

(use-fixtures :each test-database)

(deftest adding-messages
  (testing "adding message with invalid class"
    (is (thrown? IllegalArgumentException (add-message! "Uma mensagem" :some-class :training)))))

(deftest counting-messages
  (add-message! "Outra mensagem" :offensive :training)
  (testing "counting all messages"
    (is (= 2 (count-messages :training))))

  (testing "counting messages of a class"
    (is (= 1 (count-messages :offensive :training)))))

(deftest messages-from-subset
  (add-message! "Outra mensagem" :ok :test)
  (testing "get messages from :training subset"
    (let [msgs (messages-from :training)]
      (is (= 1 (count msgs)))
      (is (= "Uma mensagem" (:message (first msgs))))))

  (testing "get messages from :test subset"
    (let [msgs (messages-from :test)]
      (is (= 1 (count msgs)))
      (is (= "Outra mensagem" (:message (first msgs)))))))

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