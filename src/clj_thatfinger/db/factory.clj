(ns clj-thatfinger.db.factory
  (:import  [clj_thatfinger.db.memory_db MemoryDB]))

(defn make-memory-db
  "Creates a new instance of MemoryDB."
  [settings]
  (let [item-atom (atom '())
        feature-atom (atom {})]
    (MemoryDB. settings item-atom feature-atom)))