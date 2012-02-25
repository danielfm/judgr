(ns clj-thatfinger.test.extractor.brazilian-extractor
  (:use [clj-thatfinger.extractor.brazilian-extractor]
        [clj-thatfinger.factory]
        [clj-thatfinger.settings]
        [clojure.test]))


(def extractor
  (use-extractor
   (update-settings settings
                    [:extractor :type] :brazilian-text-extractor)))

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