(ns clj-thatfinger.stemmer.brazilian-stemmer
  (:import [org.apache.lucene.analysis.tokenattributes CharTermAttribute])
  (:import [org.apache.lucene.analysis.br BrazilianAnalyzer])
  (:import [org.apache.lucene.util Version])
  (:import [java.io StringReader]))

(def analyzer (BrazilianAnalyzer. Version/LUCENE_30))

(defn- basic-stem [s]
  (let [stream (.tokenStream analyzer "text" (StringReader. s))]
    (loop [tokens []]
      (if-not (.incrementToken stream)
        tokens
        (recur (conj tokens (.term (.getAttribute stream CharTermAttribute))))))))

;; TODO: Remove miguxês

(defn stem [s]
  (basic-stem s))