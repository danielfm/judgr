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

(deftest update-word-class-count
  (testing "increment class counter"
    (add-message! "Outra mensagem" :ok)
    (let [word (get-word "mensag")]
      (is (= 2 (:total word)))
      (is (= 2 (:ok (:classes word))))))

  (testing "start counter for new class"
    (add-message! "Mais uma mensagem" :offensive)
    (let [word (get-word "mensag")]
      (is (= '(:offensive :ok) (keys (:classes word))))
      (is (= 1 (:offensive (:classes word)))))))