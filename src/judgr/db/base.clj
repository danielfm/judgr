(ns judgr.db.base)

(defmacro ensure-valid-class
  "Throws an exception if class is not a valid class. Otherwise, run the code
in body."
  [settings class & body]
  `(if ((set (:classes ~settings)) ~class)
     (do ~@body)
     (throw (IllegalArgumentException. (str "Invalid class " ~class)))))

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

  (get-connection [db]
    "Returns the object that represents the connection to the database."))
