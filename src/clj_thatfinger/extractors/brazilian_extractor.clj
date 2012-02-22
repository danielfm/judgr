(ns clj-thatfinger.extractors.brazilian-extractor
  (:use [clj-thatfinger.extractors.base])
  (:require [clojure.string :as str])
  (:import  [java.io StringReader]
            [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
            [org.apache.lucene.analysis.br BrazilianAnalyzer]
            [org.apache.lucene.util Version]))

(def analyzer (BrazilianAnalyzer. Version/LUCENE_30))

(defn remove-repeated-chars
  "Removes long sequences of repeated chars in string s."
  [s]
  (str/replace s #"(\w)\1{2,}" "$1$1"))

(deftype BrazilianTextExtractor []
  FeatureExtractor
  (extract-features [fe item]
    (let [stream (.tokenStream analyzer "text" (StringReader. (remove-repeated-chars item)))]
      (loop [tokens []]
        (if-not (.incrementToken stream)
          (set tokens)
          (recur (conj tokens (.term (.getAttribute stream CharTermAttribute)))))))))