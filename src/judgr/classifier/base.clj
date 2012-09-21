(ns judgr.classifier.base)

(defprotocol Classifier
  "Protocol for training and evaluating a probabilistic classifier."

  (train! [c item class]
    "Labels item with class.")

  (train-all! [c map] [c items class]
    "Labels all items with class. If class is not provided, each item is a map
in the form {:item item :class class}.")

  (classify [c item]
    "Returns the class with the highest probability for item.")

  (probabilities [c item]
    "Returns the probabilities of item for each possible class."))

(defprotocol NaiveBayes
  "Protocol for calculating probabilities according to the Bayes rule."

  (class-probability [c class]
    "Returns the probability of an item to be part of class.")

  (feature-probability [c feature]
    "Returns the probability of a feature.")

  (feature-probability-given-class [c feature class]
    "Returns the probability of feature given the item is classified
as class.")

  (class-probability-given-item [c class item]
    "Returns the probability that item is classified as class."))