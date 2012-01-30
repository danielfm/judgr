(ns clj-thatfinger.settings)

(def stemmer-module 'brazilian-stemmer)
(def db-module 'mongodb)

;; MongoDB settings
(def ^:dynamic *mongodb-database* "thatfinger")
(def ^:dynamic *mongodb-host* "127.0.0.1")
(def ^:dynamic *mongodb-port* 27017)

(def ^:dynamic *mongodb-auth* false)
(def ^:dynamic *mongodb-username* "")
(def ^:dynamic *mongodb-password* "")