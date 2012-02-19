(ns clj-thatfinger.settings)

;; Default modules
(def ^:dynamic *stemmer-module* 'brazilian-stemmer)
(def ^:dynamic *db-module* 'memory-db)

;; All possible classes and their corresponding thresholds
(def ^:dynamic *classes* {:ok        {:threshold 1.5}
                          :offensive {:threshold 2.5}})

;; Messages are flagged with this class when threshold validation fails
(def ^:dynamic *class-unknown* :unknown)

;; Whether probability of any given class is unbiased, e.g., always equal
(def ^:dynamic *classes-unbiased* false)

;; Smoothing used to compensate for unknown words
(def ^:dynamic *smoothing-enabled* true)
(def ^:dynamic *smoothing-factor* 1)

;; MongoDB settings
(def ^:dynamic *mongodb-database* "thatfinger")
(def ^:dynamic *mongodb-host* "127.0.0.1")
(def ^:dynamic *mongodb-port* 27017)
(def ^:dynamic *mongodb-auth* false)
(def ^:dynamic *mongodb-username* "")
(def ^:dynamic *mongodb-password* "")