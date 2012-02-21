(ns clj-thatfinger.settings)

;; Default modules
(def ^:dynamic *stemmer-module* 'brazilian-stemmer)
(def ^:dynamic *db-module* 'mongodb)

;; All possible classes and their corresponding thresholds
(def ^:dynamic *classes* {:ok        {:threshold 1.2}
                          :offensive {:threshold 2.5}})

;; Whether the class with highest probability must pass the threshold
;; validation test
(def ^:dynamic *threshold-enabled* true)

;; Messages are flagged with this class when threshold validation fails
(def ^:dynamic *class-unknown* :unknown)

;; Whether probability of any given class is unbiased, e.g., always equal
(def ^:dynamic *classes-unbiased* false)

;; Smoothing used to compensate for unknown words
(def ^:dynamic *smoothing-factor* 1)

;; Default subset where messages and words are stored in
(def ^:dynamic *default-subset* :training)

;; MongoDB settings
(def ^:dynamic *mongodb-database* "thatfinger")
(def ^:dynamic *mongodb-host* "127.0.0.1")
(def ^:dynamic *mongodb-port* 27017)
(def ^:dynamic *mongodb-auth* false)
(def ^:dynamic *mongodb-username* "")
(def ^:dynamic *mongodb-password* "")