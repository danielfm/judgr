(ns clj-thatfinger.db.mongodb
  (:import [java.util Date])
  (:require [somnium.congomongo :as mongodb])
  (:use [clj-thatfinger.settings]
        [clj-thatfinger.extractors.default-extractor]))

(defn memory-module-name
  "Returns a name that describes this module."
  []
  "mongodb")

(defn- update-feature!
  "Updates the statistics of a feature according to the class."
  [feature class]
  (when (nil? (*classes* class))
    (throw (IllegalArgumentException. "Invalid class")))
  (mongodb/update! :features {:feature feature}
                   {:$inc {:total 1
                           (str "classes." (name class)) 1}}))

(defn add-item!
  "Stores an item indicating its class."
  [item class]
  (let [features (extract-features item)]
    (doall (map #(update-feature! % class) features))
    (mongodb/insert! :items {:item item
                             :features features
                             :created-at (Date.)
                             :class class})))

(defn get-items
  "Returns all items."
  []
  (mongodb/fetch :items))

(defn get-feature
  "Returns information about a feature."
  [feature]
  (mongodb/fetch-one :features
                     :where {:feature feature}))

(defn count-items
  "Returns the total number of items."
  []
  (mongodb/fetch-count :items))

(defn count-items-of
  "Returns the number of items that belong to a class."
  [class]
  (mongodb/fetch-count :items
                       :where {:class class}))

(defn count-features
  "Returns the total number of features."
  []
  (mongodb/fetch-count :features))

(defn- authenticate
  "Authenticates against the specified MongoDB connection."
  [conn]
  (when *mongodb-auth*
    (mongodb/authenticate conn *mongodb-username* *mongodb-password*)))

(defn- ensure-indexes!
  "Creates all necessary MongoDB indexes."
  []
  (mongodb/add-index! :items [:class])
  (mongodb/add-index! :features [:feature] :unique true))

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