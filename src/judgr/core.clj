(ns judgr.core
  (:require [judgr.db [mongo-db :as mongo-db]]
            [judgr.db.memory-db]
            [judgr.extractor.brazilian-extractor]
            [judgr.extractor.english-extractor]
            [judgr.classifier.default-classifier])

  (:import  [judgr.db.memory_db MemoryDB]
            [judgr.db.mongo_db MongoDB]
            [judgr.extractor.brazilian_extractor BrazilianTextExtractor]
            [judgr.extractor.english_extractor EnglishTextExtractor]
            [judgr.classifier.default_classifier DefaultClassifier]))

;;
;; DBs
;;
(defmulti db-from #(-> % :database :type))

(defmethod db-from :memory [settings]
  (let [item-atom (atom '[])
        feature-atom (atom {})]
    (MemoryDB. settings item-atom feature-atom)))

(defmethod db-from :mongo [settings]
  (let [conn (mongo-db/create-connection! settings)]
    (MongoDB. settings conn)))


;;
;; Extractors
;;
(defmulti extractor-from #(-> % :extractor :type))

(defmethod extractor-from :brazilian-text [settings]
  (BrazilianTextExtractor.))

(defmethod extractor-from :english-text [settings]
  (EnglishTextExtractor.))


;;
;; Classifiers
;;
(defmulti classifier-from #(-> % :classifier :type))

(defmethod classifier-from :default [settings]
  (let [db (db-from settings)
        extractor (extractor-from settings)]
    (DefaultClassifier. settings db extractor)))