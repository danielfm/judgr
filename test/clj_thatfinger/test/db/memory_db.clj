(ns clj-thatfinger.test.db.memory-db
  (:use clj-thatfinger.db.memory-db)
  (:use clojure.test))

(defn test-database [f]
  (binding [clj-thatfinger.db.memory-db/*words* (atom {})
            clj-thatfinger.db.memory-db/*messages* (atom {})]
    (add-message! "Uma mensagem" :ok)
    (f)))

(use-fixtures :each test-database)

(deftest adding-messages
  (testing "adding message with invalid class"
    (is (thrown? IllegalArgumentException
                 (add-message! "Uma mensagem" :some-class)))))

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
      (is (= "mensag" (:word word))))))

(deftest get-word-info
  (add-message! "Sua mensagem" :ok)
  (add-message! "Outra mensagem" :ok)
  (add-message! "Mensagem do inferno" :offensive)

  (let [word (get-word "mensag")]
    (is (= 4 (-> word :total)))
    (is (= '(:offensive :ok) (-> word :classes keys)))
    (is (= 3 (-> word :classes :ok)))
    (is (= 1 (-> word :classes :offensive)))))