(ns judgr.extractor.english-extractor
  (:use [judgr.extractor.base])
  (:import  [java.io StringReader]
            [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
            [org.apache.lucene.analysis.en EnglishAnalyzer]
            [org.apache.lucene.util Version]))

(def analyzer (EnglishAnalyzer. Version/LUCENE_30))

(defn- extractor-settings
  "Returns the settings specific for this extractor."
  [settings]
  (-> settings :extractor :english-text))

(deftype EnglishTextExtractor [settings]
  FeatureExtractor
  (extract-features [fe item]
    (let [stream (.tokenStream analyzer "text" (StringReader. item))]
      (loop [tokens []]
        (if-not (.incrementToken stream)
          (if (:remove-duplicates? (extractor-settings settings))
            (set tokens) tokens)
          (recur (conj tokens
                       (.term (.getAttribute stream CharTermAttribute)))))))))