(ns clj-thatfinger.db.base)

(defprotocol FeatureDB
  "Protocol for reading/writing feature information from/to a database."

  (add-item! [db item class]
    "Stores an item indicating its class.")

  (add-feature! [db item feature class]
    "Stores an item's feature indicating its class.")

  (count-features [db]
    "Returns the total number of features.")

  (get-feature [db feature]
    "Returns information about a feature.")

  (get-items [db]
    "Returns all items.")

  (count-items [db]
    "Returns the total number of items.")

  (count-items-of-class [db class]
    "Returns the number of items that belong to a class."))

(defprotocol ConnectionBasedDB
  "Databases that require a connection to work must implement this protocol."

  (connect! [db]
    "Connects to the database.")

  (is-connected? [db]
    "Returns whether the database connection is open.")

  (disconnect! [db]
    "Disconnects from the database.")

  (get-connection [db]
    "Returns the object that represents the connection to the database."))