(ns clj-thatfinger.test.extractors.brazilian-extractor
  (:use [clj-thatfinger.extractors.brazilian-extractor])
  (:use [clj-thatfinger.extractors.factory])
  (:use [clojure.test]))

(def extractor (make-brazilian-extractor))

(deftest brazilian-extractor
  (testing "remove repeated characters"
    (is (= "aabbcdd" (remove-repeated-chars "aaaaabbbcddd"))))

  (testing "extract features"
    (is (= #{"pont" "vist" "gramatical" "term" "sublinh" "esta" "corret" "empreg"}
           (.extract-features extractor "Do ponto de vista gramatical, os termos sublinhados estão corretamente empregados!")))

    (testing "with repeated characters removed"
      (is (= #{"ola" "lind" "kk" "adoor"}
             (.extract-features extractor "Olaaaaa linda, kkkkk adooooro"))))

    (testing "with repeated words"
      (is (= #{"ola"}
             (.extract-features extractor "Ola, olaaa"))))))