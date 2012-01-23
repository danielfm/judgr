(ns clj-thatfinger.test.stemmer.brazilian-stemmer
  (:use [clj-thatfinger.stemmer.brazilian-stemmer])
  (:use [clojure.test]))

(deftest brazilian-stemmer
  (testing "basic word stemming"
    (is (= ["pont" "vist" "gramatical" "term" "sublinh" "esta" "corret" "empreg"]
           (stem "Do ponto de vista gramatical, os termos sublinhados estão corretamente empregados!")))))