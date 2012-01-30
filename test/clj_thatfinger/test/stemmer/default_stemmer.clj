(ns clj-thatfinger.test.stemmer.default-stemmer
  (:use [clj-thatfinger.stemmer.default-stemmer])
  (:use [clojure.test]))

(defn dummy-module [f]
  (binding [clj-thatfinger.settings/*stemmer-module* 'dummy_stemmer]
    (f)))

(use-fixtures :once dummy-module)

(deftest module-require
  (testing "require module defined in settings"
    (= "dummy stemmer" (stem "echo"))))