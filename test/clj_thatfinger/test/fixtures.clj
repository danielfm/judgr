(ns clj-thatfinger.test.fixtures
  (:use [clj-thatfinger.test.utils])
  (:require [somnium.congomongo]
            [clj-thatfinger.db.memory-db]
            [clj-thatfinger.db.mongodb]))

(def training-set
  [["Você é um diabo, mesmo." :ok]
   ["Sai de ré, capeta." :offensive]
   ["Vai pro inferno, diabo!" :offensive]
   ["Sua filha é uma diaba, doido." :offensive]])

(def-fixture smoothing [factor]
  (binding [clj-thatfinger.settings/*smoothing-factor* factor]
    (test-body)))

(def-fixture threshold [classes]
  (binding [clj-thatfinger.settings/*threshold-enabled* true
            clj-thatfinger.settings/*class-unknown* :unknown
            clj-thatfinger.settings/*classes* classes]
    (test-body)))

(def-fixture without-threshold []
  (binding [clj-thatfinger.settings/*threshold-enabled* false]
    (test-body)))

(def-fixture test-memory-db []
  (binding [clj-thatfinger.settings/*db-module* 'clj-thatfinger.db.memory-db
            clj-thatfinger.db.memory-db/*items* (atom {})
            clj-thatfinger.db.memory-db/*features* (atom {})]
    (doall (map #(apply clj-thatfinger.db.memory-db/add-item! %) training-set))
    (test-body)))

(def-fixture test-mongo-db []
  (binding [clj-thatfinger.settings/*db-module* 'clj-thatfinger.db.mongodb
            clj-thatfinger.settings/*mongodb-database* "thatfinger-test"]
    (clj-thatfinger.db.mongodb/create-connection!)
    (somnium.congomongo/destroy! :features {})
    (somnium.congomongo/destroy! :items {})
    (doall (map #(apply clj-thatfinger.db.mongodb/add-item! %) training-set))
    (test-body)))

(def-fixture confusion-matrix []
  (let [conf-matrix {:a {:a 25 :b 5  :c 2}
                     :b {:a 3  :b 32 :c 4}
                     :c {:a 1  :b 0  :c 15}}]
    (test-body)))