(ns clj-thatfinger.db.mongodb
  (:import [java.util Date])
  (:require [somnium.congomongo :as mongodb])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(defn- update-word!
  "Updates the statistics of a word according to the offensive? flag."
  [word offensive?]
  (let [inc-offensive (if offensive? 1 0)]
    (mongodb/update! :words {:word word}
                   {:$inc {:total 1,
                           :total-offensive inc-offensive}})))

(defn add-message!
  "Stores a message indicating whether it's offensive or not."
  [message & [offensive?]]
  (let [words (stem message)]
    (doall (map #(update-word! % offensive?) words))
    (mongodb/insert! :messages {:message message
                              :words words
                              :created-at (Date.)
                              :offensive? offensive?})))

(defn get-word
  "Returns information about a word."
  [word]
  (mongodb/fetch-one :words
                   :where {:word word}))

(defn count-offensive-messages
  "Returns the number of messages flagged as offensive."
  []
  (mongodb/fetch-count :messages
                     :where {:offensive? true}))

(defn count-messages
  "Returns the total number of messages."
  []
  (mongodb/fetch-count :messages))

(defn- authenticate
  "Authenticates against the specified MongoDB connection."
  [conn]
  (when *mongodb-auth*
    (mongodb/authenticate conn *mongodb-username* *mongodb-password*)))

(defn- ensure-indexes!
  "Creates all necessary MongoDB indexes."
  []
  (mongodb/add-index! :messages [:offensive?])
  (mongodb/add-index! :words [:word] :unique true))

(defn create-connection!
  "Creates a connection to MongoDB server."
  []
  (let [conn (mongodb/make-connection *mongodb-database*
                                      :host *mongodb-host*
                                      :port *mongodb-port*)]
    (authenticate conn)
    (mongodb/set-connection! conn)
    (ensure-indexes!)))

(create-connection!)