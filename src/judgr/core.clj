(ns judgr.core
  (:require [judgr.db.memory-db]
            [judgr.extractor.brazilian-extractor]
            [judgr.extractor.english-extractor]
            [judgr.classifier.default-classifier])

  (:import  [judgr.db.memory_db MemoryDB]
            [judgr.extractor.brazilian_extractor BrazilianTextExtractor]
            [judgr.extractor.english_extractor EnglishTextExtractor]
            [judgr.classifier.default_classifier DefaultClassifier]))

(defmulti db-from #(-> % :database :type))
(defmulti extractor-from #(-> % :extractor :type))
(defmulti classifier-from #(-> % :classifier :type))

(defmethod db-from :memory [settings]
  (let [items (ref [])
        features (ref {})]
    (MemoryDB. settings items features)))

(defmethod classifier-from :default [settings]
  (let [db (db-from settings)
        extractor (extractor-from settings)]
    (DefaultClassifier. settings db extractor)))

(defmethod extractor-from :brazilian-text [settings]
  (BrazilianTextExtractor. settings))

(defmethod extractor-from :english-text [settings]
  (EnglishTextExtractor. settings))