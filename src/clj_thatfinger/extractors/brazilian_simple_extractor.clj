(ns clj-thatfinger.extractors.brazilian-simple-extractor
  (:import  [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
            [org.apache.lucene.analysis.br BrazilianAnalyzer]
            [org.apache.lucene.util Version]
            [java.io StringReader])
  (:require [clojure.string :as str]))

(def analyzer (BrazilianAnalyzer. Version/LUCENE_30))

(defn extractor-module-name
  "Returns a name that describes this module."
  []
  "brazilian-simple-extractor")

(defn remove-repeated-chars
  "Removes long sequences of repeated chars in string s."
  [s]
  (str/replace s #"(\w)\1{2,}" "$1$1"))

(defn extract-features
  "Returns a set of features extracted from string s."
  [s]
  (let [stream (.tokenStream analyzer "text" (StringReader. (remove-repeated-chars s)))]
    (loop [tokens []]
      (if-not (.incrementToken stream)
        (set tokens)
        (recur (conj tokens (.term (.getAttribute stream CharTermAttribute))))))))