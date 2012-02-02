(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

;; Loads the database module configured in settings
(require ['clj-thatfinger.db [*db-module* :as 'db]])

(defn add-message!
  "Stores a message indicating its category."
  [message cat]
  (db/add-message! message cat))

(defn get-word
  "Returns information about a word."
  [word]
  (db/get-word word))

(defn count-messages-of-category
  "Returns the number of messages of a category cat."
  [cat]
  (db/count-messages-of-category cat))

(defn count-messages
  "Returns the total number of messages."
  []
  (db/count-messages))

(defn count-words
  "Returns the total number of words."
  []
  (db/count-words))