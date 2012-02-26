(ns clj-thatfinger.classifier.base)

(defprotocol Classifier
  ""

  (train! [c item class]
    "Labels item with class.")

  (classify [c item]
    "Returns the class with the highest probability for item that passes the
threshold validation.")

  (probabilities [c item]
    "Returns the probabilities of item for each possible class."))