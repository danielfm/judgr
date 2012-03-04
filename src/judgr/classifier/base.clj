(ns judgr.classifier.base)

(defprotocol Classifier
  ""

  (train! [c item class]
    "Labels item with class.")

  (classify [c item]
    "Returns the class with the highest probability for item.")

  (probabilities [c item]
    "Returns the probabilities of item for each possible class."))

(defprotocol NaiveBayes
  ""

  (class-probability [c class]
    "Returns the probability of an item to be part of class.")

  (feature-probability-given-class [c feature class]
    "Returns the probability of feature given the item is classified
as class.")

  (class-probability-given-feature [c class feature]
    "Returns the probability of class given feature is present.")

  (class-probability-given-item [c class item]
    "Returns the probability that item is classified as class."))