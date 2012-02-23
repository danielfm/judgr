(ns clj-thatfinger.extractor.factory
  (:require [clj-thatfinger.extractor [brazilian-extractor :as brazilian-extractor]])
  (:import  [clj_thatfinger.extractor.brazilian_extractor BrazilianTextExtractor]))

(defn make-brazilian-extractor
  "Creates a new instance of BrazilianTextExtractor."
  []
  (BrazilianTextExtractor.))