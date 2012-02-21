(ns clj-thatfinger.test.db.default-db
  (:use [clj-thatfinger.db.default-db])
  (:use [clojure.test]))

(deftest module-require
  (testing "require module defined in settings"
    (is (= "mongodb" (memory-module-name)))))