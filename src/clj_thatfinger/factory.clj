(ns clj-thatfinger.factory
  (:require [clj-thatfinger.db [mongo-db :as mongo-db]]
            [clj-thatfinger.db.memory-db]
            [clj-thatfinger.extractor.brazilian-extractor]
            [clj-thatfinger.classifier.default-classifier])

  (:import  [clj_thatfinger.db.memory_db MemoryDB]
            [clj_thatfinger.db.mongo_db MongoDB]
            [clj_thatfinger.extractor.brazilian_extractor BrazilianTextExtractor]))

;;
;; DBs
;;
(defmulti use-db #(-> % :database :type))

(defmethod use-db :memory [settings]
  (let [item-atom (atom '[])
        feature-atom (atom {})]
    (MemoryDB. settings item-atom feature-atom)))

(defmethod use-db :mongo [settings]
  (let [conn (mongo-db/create-connection! settings)]
    (MongoDB. settings conn)))


;;
;; Extractors
;;
(defmulti use-extractor #(-> % :extractor :type))

(defmethod use-extractor :brazilian-text [settings]
  (BrazilianTextExtractor.))