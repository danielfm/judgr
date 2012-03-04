(ns judgr.extractor.english-extractor
  (:use [judgr.extractor.base])
  (:require [clojure.string :as str])
  (:import  [java.io StringReader]
            [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
            [org.apache.lucene.analysis.en EnglishAnalyzer]
            [org.apache.lucene.util Version]))

(def analyzer (EnglishAnalyzer. Version/LUCENE_30))

(deftype EnglishTextExtractor []
  FeatureExtractor
  (extract-features [fe item]
    (let [stream (.tokenStream analyzer "text" (StringReader. item))]
      (loop [tokens []]
        (if-not (.incrementToken stream)
          (set tokens)
          (recur (conj tokens (.term (.getAttribute stream CharTermAttribute)))))))))