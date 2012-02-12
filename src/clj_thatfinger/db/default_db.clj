(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

;; Loads the database module configured in settings
(require ['clj-thatfinger.db [*db-module* :as 'db]])

(defn module-name
  ""
  []
  (db/module-name))

(defn add-message!
  "Stores a message indicating its class."
  [message cls]
  (db/add-message! message cls))

(defn get-word
  "Returns information about a word."
  [word]
  (db/get-word word))

(defn count-messages
  "Returns the total number of messages."
  ([] (db/count-messages))
  ([cls] (db/count-messages cls)))

(defn count-words
  "Returns the total number of words."
  []
  (db/count-words))