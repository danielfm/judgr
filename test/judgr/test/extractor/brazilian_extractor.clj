(ns judgr.test.extractor.brazilian-extractor
  (:use [judgr.extractor.brazilian-extractor]
        [judgr.core]
        [judgr.test.util]
        [judgr.settings]
        [clojure.test])
  (:import [judgr.extractor.brazilian_extractor BrazilianTextExtractor]))

(def-fixture remove-duplicates [b]
  (let [new-settings (update-settings settings
                                      [:extractor :type] :brazilian-text
                                      [:extractor :brazilian-text] {:remove-duplicates? b})
        extractor (extractor-from new-settings)]
    (test-body)))

(deftest ensure-brazilian-text-extractor
  (with-fixture remove-duplicates [true]
    (is (instance? BrazilianTextExtractor extractor))))

(deftest brazilian-extractor
  (testing "extract features"
    (testing "removing duplicates"
      (with-fixture remove-duplicates [true]
        (is (= #{"pont" "term" "empreg" "gramatical" "vist" "sublinh" "corret" "esta"}
           (.extract-features extractor "Do ponto de vista gramatical, gramaticalmente os termos sublinhados estão corretamente empregados!")))))

    (testing "leaving duplicates"
      (with-fixture remove-duplicates [false]
        (is (= ["pont" "vist" "gramatical" "gramatical" "term" "sublinh" "esta" "corret" "empreg"]
           (.extract-features extractor "Do ponto de vista gramatical, gramaticalmente os termos sublinhados estão corretamente empregados!")))))))
