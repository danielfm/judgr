(ns clj-thatfinger.db.factory
  (:require [clj-thatfinger.db [mongo-db :as mongo-db]])
  (:import  [clj_thatfinger.db.memory_db MemoryDB]
            [clj_thatfinger.db.mongo_db MongoDB]))

(defn make-memory-db
  "Creates a new instance of MemoryDB."
  [settings]
  (let [item-atom (atom '())
        feature-atom (atom {})]
    (MemoryDB. settings item-atom feature-atom)))

(defn make-mongo-db
  "Creates a new instance of MongoDB."
  [settings]
  (let [conn (mongo-db/create-connection! settings)]
    (MongoDB. settings conn)))