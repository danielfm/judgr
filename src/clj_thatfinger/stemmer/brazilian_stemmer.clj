(ns clj-thatfinger.stemmer.brazilian-stemmer
  (:import  [org.apache.lucene.analysis.tokenattributes CharTermAttribute])
  (:import  [org.apache.lucene.analysis.br BrazilianAnalyzer])
  (:import  [org.apache.lucene.util Version])
  (:import  [java.io StringReader])
  (:require [clojure.string :as str]))

(def analyzer (BrazilianAnalyzer. Version/LUCENE_30))

(defn remove-repeated-chars
  "Decreases long sequences of repeated chars in s."
  [s]
  (str/replace s #"(\w)\1{2,}" "$1$1"))

(defn stem [s]
  "Returns an array with the stemming output for string s."
  (let [stream (.tokenStream analyzer "text" (StringReader. (remove-repeated-chars s)))]
    (loop [tokens []]
      (if-not (.incrementToken stream)
        tokens
        (recur (conj tokens (.term (.getAttribute stream CharTermAttribute))))))))