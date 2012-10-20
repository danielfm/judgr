(ns judgr.test.extractor.english-extractor
  (:use [judgr.extractor.english-extractor]
        [judgr.core]
        [judgr.test.util]
        [judgr.settings]
        [clojure.test])
  (:import [judgr.extractor.english_extractor EnglishTextExtractor]))

(def-fixture remove-duplicates [b]
  (let [new-settings (update-settings settings
                                      [:extractor :type] :english-text
                                      [:extractor :english-text] {:remove-duplicates? b})
        extractor (extractor-from new-settings)]
    (test-body)))

(deftest ensure-english-text-extractor
  (with-fixture remove-duplicates [true]
    (is (instance? EnglishTextExtractor extractor))))

(deftest english-extractor
  (testing "extract features"
    (testing "removing duplicates"
      (with-fixture remove-duplicates [true]
        (is (= #{"syntax" "workshop" "about" "basic" "english" "idea" "review" "few"}
           (.extract-features extractor "This workshop is a review of a few reviewed basic ideas about English syntax")))))

    (testing "leaving duplicates"
      (with-fixture remove-duplicates [false]
        (is (= ["workshop" "review" "few" "review" "basic" "idea" "about" "english" "syntax"]
           (.extract-features extractor "This workshop is a review of a few reviewed basic ideas about English syntax")))))))