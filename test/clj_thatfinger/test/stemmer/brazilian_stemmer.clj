(ns clj-thatfinger.test.stemmer.brazilian-stemmer
  (:use [clj-thatfinger.stemmer.brazilian-stemmer])
  (:use [clojure.test]))

(deftest brazilian-stemmer
  (testing "remove repeated characters"
    (is (= "aabbcdd" (remove-repeated-chars "aaaaabbbcddd"))))

  (testing "word stemming"
    (is (= ["pont" "vist" "gramatical" "term" "sublinh" "esta" "corret" "empreg"]
           (stem "Do ponto de vista gramatical, os termos sublinhados estão corretamente empregados!")))

    (testing "with repeated characters removed"
      (is (= ["ola" "lind" "kk" "adoor"]
             (stem "Olaaaaa linda, kkkkk adooooro"))))))