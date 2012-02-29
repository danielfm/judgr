(ns clj-thatfinger.classifier.default-classifier
  (:use [clj-thatfinger.classifier.base]
        [clj-thatfinger.probability]))

(defn- classifier-settings
  "Returns the settings specific for this classifier."
  [settings]
  (-> settings :classifier :default))

(defn probability
  "Calculates the probability with smoothing if it's enabled in settings."
  [cls-count total-count occurrences settings]
    (let [factor (:smoothing-factor (classifier-settings settings))
          cls-count (+ (or cls-count 0) factor)
          total-count (+ (or total-count 0) (* occurrences factor))]
      (/ cls-count total-count)))

(deftype DefaultClassifier [settings db extractor]
  Classifier

  (train! [c item class]
    (let [features (.extract-features extractor item)]
      (.add-item! db item class)
      (doall (map #(.add-feature! db item % class) features))))

  (probabilities [c item]
    (let [classes (:classes settings)]
      (zipmap classes (map #(.class-probability-given-item c % item) classes))))

  (classify [c item]
    (let [classifier-settings (classifier-settings settings)
          probs (reverse (sort-by val (.probabilities c item)))
          [first-prob second-prob & _] probs]
      (if (or (not (:threshold? classifier-settings))
              (<= (* (val second-prob)
                     ((-> classifier-settings :thresholds) (key first-prob)))
                  (val first-prob)))
        (key first-prob)
        (:unknown-class classifier-settings))))

  NaiveBayes

  (class-probability [c class]
    (let [class-count (count (:classes settings))]
      (if (:unbiased? (classifier-settings settings))
        (/ 1 class-count)
        (probability (.count-items-of-class db class)
                     (.count-items db)
                     class-count
                     settings))))

  (feature-probability-given-class [c feature class]
    (let [feature (.get-feature db feature)]
      (probability  (-> feature :classes class)
                    (.count-items-of-class db class)
                    (.count-features db)
                    settings)))

  (class-probability-given-feature [c class feature]
    (let [prior (.class-probability c class)
          posterior (.feature-probability-given-class c feature class)
          total-probs (reduce +
                        (map #(* (.feature-probability-given-class c feature %)
                                 (.class-probability c %))
                             (:classes settings)))]
      (if (zero? total-probs)
        0
        (/ (* prior posterior) total-probs))))

  (class-probability-given-item [c class item]
    (let [features (.extract-features extractor item)]
      (fisher-prob (map #(.class-probability-given-feature c class %)
                        features)))))