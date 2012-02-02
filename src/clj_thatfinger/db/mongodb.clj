(ns clj-thatfinger.db.mongodb
  (:import [java.util Date])
  (:require [somnium.congomongo :as mongodb])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(defn- update-word!
  "Updates the statistics of a word according to the category cat."
  [word cat]
  (mongodb/update! :words {:word word}
                   {:$inc {:total 1
                           cat 1}}))

(defn add-message!
  "Stores a message indicating its category."
  [message cat]
  (let [words (stem message)]
    (doall (map #(update-word! % cat) words))
    (mongodb/insert! :messages {:message message
                                :words words
                                :created-at (Date.)
                                :category cat})))

(defn get-word
  "Returns information about a word."
  [word]
  (mongodb/fetch-one :words
                     :where {:word word}))

(defn count-messages-of-category
  "Returns the number of messages of a category cat."
  [cat]
  (mongodb/fetch-count :messages
                       :where {:category cat}))

(defn count-messages
  "Returns the total number of messages."
  []
  (mongodb/fetch-count :messages))

(defn count-words
  "Returns the total number of words."
  []
  (mongodb/fetch-count :words))

(defn- authenticate
  "Authenticates against the specified MongoDB connection."
  [conn]
  (when *mongodb-auth*
    (mongodb/authenticate conn *mongodb-username* *mongodb-password*)))

(defn- ensure-indexes!
  "Creates all necessary MongoDB indexes."
  []
  (mongodb/add-index! :messages [:category])
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