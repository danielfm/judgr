(ns clj-thatfinger.test.db.default-db
  (:use [clj-thatfinger.db.default-db])
  (:use [clojure.test]))

(defn dummy-module [f]
  (binding [clj-thatfinger.settings/*db-module* 'dummy-db]
    (f)))

(use-fixtures :once dummy-module)

(deftest module-require
  (testing "require module defined in settings"
    (= "dummy db" (add-message! "Some message"))))