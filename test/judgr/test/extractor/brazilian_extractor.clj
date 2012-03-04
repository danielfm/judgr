(ns judgr.test.extractor.brazilian-extractor
  (:use [judgr.extractor.brazilian-extractor]
        [judgr.core]
        [judgr.settings]
        [clojure.test])
  (:import [judgr.extractor.brazilian_extractor BrazilianTextExtractor]))

(def extractor
  (extractor-from
   (update-settings settings
                    [:extractor :type] :brazilian-text)))

(deftest ensure-brazilian-text-extractor
  (is (instance? BrazilianTextExtractor extractor)))

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
