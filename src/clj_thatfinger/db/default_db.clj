(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

;; Loads the database module configured in settings
(require ['clj-thatfinger.db [*db-module* :as 'db]])

(defn module-name
  "Returns a name that describes this module."
  []
  (db/module-name))

(defn add-message!
  "Stores a message indicating its class."
  [message cls subset]
  (db/add-message! message cls subset))

(defn get-word
  "Returns information about a word."
  [word]
  (db/get-word word))

(defn messages-from
  "Returns all messages from a given training subset."
  [subset]
  (db/messages-from subset))

(defn count-messages
  "Returns the total number of messages."
  ([subset] (db/count-messages subset))
  ([cls subset] (db/count-messages cls subset)))

(defn count-words
  "Returns the total number of words."
  [subset]
  (db/count-words subset))