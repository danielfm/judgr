(ns clj-thatfinger.extractors.base)

(defprotocol FeatureExtractor
  "Protocol for extracting features of a classifiable item."
  (extract-features [fe item] "Extracts a seq of features from item"))