(ns judgr.classifier.default-classifier
  (:use [judgr.classifier.base]))

(defn- classifier-settings
  "Returns the settings specific for this classifier."
  [settings]
  (-> settings :classifier :default))

(defn- threshold-for-class
  "Returns the threshold for the given class. Raises an exception if there's
no threshold config for that class."
  [classifier-settings cls]
  (if-let [threshold (cls (-> classifier-settings :thresholds))]
    threshold
    (throw (IllegalArgumentException.
            (str "Please specify [:classifier :default :thresholds] setting " cls)))))

(defn probability
  "Calculates the probability with smoothing if it's enabled in settings."
  [cls-count total-count occurrences settings]
    (let [factor (get (classifier-settings settings) :smoothing-factor 0)
          cls-count (+ (or cls-count 0) factor)
          total-count (+ (or total-count 0) (* occurrences factor))]
      (if (zero? total-count)
        (throw (IllegalStateException. "There is no training data"))
        (/ cls-count total-count))))

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
                     (threshold-for-class classifier-settings (key first-prob)))
                  (val first-prob)))
        (key first-prob)
        (:unknown-class classifier-settings))))

  NaiveBayes

  (class-probability [c class]
    (let [class-count (count (:classes settings))]
      (when (zero? class-count)
        (throw (IllegalStateException. "Please specify [:classes] setting")))
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

  (feature-probability [c feature]
    (probability (:total (.get-feature db feature))
                 (.count-items db)
                 (.count-features db)
                 settings))

  (class-probability-given-item [c class item]
    (let [features (.extract-features extractor item)]
      (letfn [(feature-given-class [feature]
                (.feature-probability-given-class c feature class))
              (features-prob [feature]
                (.feature-probability c feature))]
        (/ (* (.class-probability c class)
              (reduce * (map feature-given-class features)))
           (reduce * (map features-prob features)))))))