(ns clj-thatfinger.classifier.default-classifier
  (:use [clj-thatfinger.classifier.base]))

(deftype DefaultClassifier [settings db extractor]
  Classifier

  (train! [c item class]
    (let [features (.extract-features extractor item)]
      (.add-item! db item class)
      (doall (map #(.add-feature! db item % class) features))))

  (classify [c item]
    )

  (probabilities [c item]
    ))