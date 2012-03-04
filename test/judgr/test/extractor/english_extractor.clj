(ns judgr.test.extractor.english-extractor
  (:use [judgr.extractor.english-extractor]
        [judgr.core]
        [judgr.settings]
        [clojure.test])
  (:import [judgr.extractor.english_extractor EnglishTextExtractor]))

(def extractor
  (extractor-from
   (update-settings settings
                    [:extractor :type] :english-text)))

(deftest ensure-english-text-extractor
  (is (instance? EnglishTextExtractor extractor)))

(deftest english-extractor
  (testing "extract features"
    (is (= #{"syntax" "workshop" "about" "basic" "english" "idea" "review" "few"}
           (.extract-features extractor "This workshop is a review of a few basic ideas about English syntax")))

    (testing "with repeated words"
      (is (= #{"hello"}
             (.extract-features extractor "Hello, hello!"))))))