(ns clj-thatfinger.settings)

(def stemmer-module 'brazilian-stemmer)
(def db-module 'mongodb)

;; MongoDB settings
(def mongodb-database "thatfinger")
(def mongodb-host "127.0.0.1")
(def mongodb-port 27017)
(def mongodb-auth false)
(def mongodb-username "")
(def mongodb-password "")