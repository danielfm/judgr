(ns clj-thatfinger.extractors.factory
  (:require [clj-thatfinger.extractors [brazilian-extractor :as brazilian-extractor]])
  (:import  [clj_thatfinger.extractors.brazilian_extractor BrazilianTextExtractor]))

(defn make-brazilian-extractor
  "Creates a new instance of BrazilianTextExtractor."
  []
  (BrazilianTextExtractor.))