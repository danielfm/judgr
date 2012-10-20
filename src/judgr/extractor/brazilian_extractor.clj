(ns judgr.extractor.brazilian-extractor
  (:use [judgr.extractor.base])
  (:import  [java.io StringReader]
            [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
            [org.apache.lucene.analysis.br BrazilianAnalyzer]
            [org.apache.lucene.util Version]))

(def analyzer (BrazilianAnalyzer. Version/LUCENE_30))

(defn- extractor-settings
  "Returns the settings specific for this extractor."
  [settings]
  (-> settings :extractor :brazilian-text))

(deftype BrazilianTextExtractor [settings]
  FeatureExtractor
  (extract-features [fe item]
    (let [stream (.tokenStream analyzer "text" (StringReader. item))]
      (loop [tokens []]
        (if-not (.incrementToken stream)
          (if (:remove-duplicates? (extractor-settings settings))
            (set tokens) tokens)
          (recur (conj tokens
                       (.term (.getAttribute stream CharTermAttribute)))))))))