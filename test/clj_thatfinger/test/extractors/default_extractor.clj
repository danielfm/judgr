(ns clj-thatfinger.test.extractors.default-extractor
  (:use [clj-thatfinger.extractors.default-extractor])
  (:use [clojure.test]))

(deftest module-require
  (testing "require module defined in settings"
    (= "brazilian-simple-extractor" (extractor-module-name))))