(ns clj-thatfinger.db.default-db
  (:use [clj-thatfinger.settings]))

;; Loads the database module configured in settings
(require ['clj-thatfinger.db [*db-module* :as 'db]])

(defn add-message!
  "Stores a message indicating whether it's offensive or not."
  [message & [offensive?]]
  (db/add-message! message offensive?))

(defn get-word
  "Returns information about a word."
  [word]
  (db/get-word word))

(defn count-offensive-messages
  "Returns the number of messages flagged as offensive."
  []
  (db/count-offensive-messages))

(defn count-messages
  "Returns the total number of messages."
  []
  (db/count-messages))