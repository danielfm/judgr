(ns clj-thatfinger.test.extractors.brazilian-simple-extractor
  (:use [clj-thatfinger.extractors.brazilian-simple-extractor])
  (:use [clojure.test]))

(deftest brazilian-extractor
  (testing "remove repeated characters"
    (is (= "aabbcdd" (remove-repeated-chars "aaaaabbbcddd"))))

  (testing "extract features"
    (is (= #{"pont" "vist" "gramatical" "term" "sublinh" "esta" "corret" "empreg"}
           (extract-features "Do ponto de vista gramatical, os termos sublinhados estão corretamente empregados!")))

    (testing "with repeated characters removed"
      (is (= #{"ola" "lind" "kk" "adoor"}
             (extract-features "Olaaaaa linda, kkkkk adooooro"))))

    (testing "with repeated words"
      (is (= #{"ola"}
             (extract-features "Ola, olaaa"))))))