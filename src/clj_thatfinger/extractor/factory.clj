(ns clj-thatfinger.extractor.factory
  (:import  [clj_thatfinger.extractor.brazilian_extractor BrazilianTextExtractor]))

(defn make-brazilian-extractor
  "Creates a new instance of BrazilianTextExtractor."
  []
  (BrazilianTextExtractor.))