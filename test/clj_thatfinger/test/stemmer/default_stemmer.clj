(ns clj-thatfinger.test.stemmer.default-stemmer
  (:use [clj-thatfinger.stemmer.default-stemmer])
  (:use [clojure.test]))

(deftest module-require
  (testing "require module defined in settings"
    (= "brazilian-stemmer" (stemmer-module-name))))