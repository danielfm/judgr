(ns clj-thatfinger.db.mongodb
  (:import [java.util Date])
  (:require [somnium.congomongo :as mongodb])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.stemmer.default-stemmer]))

(defn module-name
  "Returns a name that describes this module."
  []
  "mongodb")

(defn- update-word!
  "Updates the statistics of a word according to the class cls."
  [word cls]
  (if (nil? (*classes* cls))
    (throw (IllegalArgumentException. "Invalid class"))
    (mongodb/update! :words {:word word}
                     {:$inc {:total 1
                             (keyword (str "classes." (name cls))) 1}})))

(defn add-message!
  "Stores a message indicating its class."
  [message cls]
  (let [words (stem message)]
    (doall (map #(update-word! % cls) words))
    (mongodb/insert! :messages {:message message
                                :words words
                                :created-at (Date.)
                                :class cls})))

(defn get-word
  "Returns information about a word."
  [word]
  (mongodb/fetch-one :words
                     :where {:word word}))

(defn count-messages
  "Returns the total number of messages of an optional class cls."
  ([]
     (mongodb/fetch-count :messages))

  ([cls]
     (mongodb/fetch-count :messages
                          :where {:class cls})))

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
  (mongodb/add-index! :messages [:class])
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