(ns clj-thatfinger.settings)

;; Default modules
(def ^:dynamic *stemmer-module* 'brazilian-stemmer)
(def ^:dynamic *db-module* 'mongodb)

(def ^:dynamic *classes-count* 2)

;; Smoothing
(def ^:dynamic *smoothing-enabled* true)
(def ^:dynamic *smoothing-factor* 1)

;; MongoDB settings
(def ^:dynamic *mongodb-database* "thatfinger")
(def ^:dynamic *mongodb-host* "127.0.0.1")
(def ^:dynamic *mongodb-port* 27017)

(def ^:dynamic *mongodb-auth* false)
(def ^:dynamic *mongodb-username* "")
(def ^:dynamic *mongodb-password* "")