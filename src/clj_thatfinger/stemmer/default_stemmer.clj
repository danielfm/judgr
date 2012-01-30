(ns clj-thatfinger.stemmer.default-stemmer
  (:use [clj-thatfinger.settings]))

;; Loads the stemmer configured in settings
(require ['clj-thatfinger.stemmer [*stemmer-module* :as 'stemmer]])

(defn stem
  "Returns a set with the stemming output for string s."
  [s]
  (stemmer/stem s))