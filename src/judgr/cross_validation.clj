(ns judgr.cross-validation
  (:use [judgr.core]
        [judgr.collection-util]
        [judgr.settings]))

(defn train-all-partitions-but!
  "Trains all chunks of items except the chunk at nth position."
  [k items classifier]
  (doall (map #(.train-all! classifier %) (remove-nth k items))))

(defn create-confusion-matrix
  "Returns an empty confusion matrix."
  [settings]
  (let [classes (:classes settings)
        entries (zipmap (conj classes (:unknown-class settings))
                        (repeat 0))]
    (zipmap classes (repeat entries))))

(defn eval-model
  "Classifies labeled items and returns a confusion matrix representing
the classification score."
  [items classifier]
  (let [chunks (partition-all 4 items)
        conf-matrix (create-confusion-matrix (.settings classifier))
        updater (fn [conf-matrix item]
                  (let [expected (:class item)
                        predicted (.classify classifier (:item item))
                        old (-> conf-matrix expected predicted)]
      (assoc-in conf-matrix [expected predicted] (inc old))))]
    (reduce aggregate-results
            (pmap #(reduce updater conf-matrix %) chunks))))

(defn- in-memory-classifier
  "Returns a new in-memory classifier based on the given one."
  [classifier]
  (let [settings (update-settings (.settings classifier)
                                  [:database :type] :memory)]
    (classifier-from settings)))

(defn k-fold-crossval
  "Performs k-fold cross validation and return a confusion matrix as a map
of maps, where the first level contains the expected classes and the second
level contains predicted classes."
  [k classifier]
  (let [subsets (partition-items k (shuffle (.get-items (.db classifier))))]
    (let [results (map (fn [i]
                         (let [mem-classifier (in-memory-classifier classifier)]
                           (train-all-partitions-but! i subsets mem-classifier)
                           (eval-model (nth subsets i) mem-classifier)))
                       (range (count subsets)))]
      (reduce aggregate-results results))))

(defn true-positives
  "Returns the number of true positives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key true-positives conf-matrix))))
  ([cls conf-matrix]
     (or (-> conf-matrix cls cls) 0)))

(defn false-positives
  "Returns the number of false positives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key false-positives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals
                (apply-to-each-key #(get-in %2 [% cls] 0)
                                   (dissoc conf-matrix cls))))))

(defn false-negatives
  "Returns the number of false negatives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key false-negatives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals
                (dissoc (apply-to-each-key #(get-in %2 [cls %] 0)
                                           (nested-dissoc conf-matrix [cls cls]))
                        cls)))))

(defn true-negatives
  "Returns the number of true negatives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key true-negatives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals
                (apply concat (map #(dissoc (val %) cls)
                                   (dissoc conf-matrix cls)))))))

(defn precision
  "Calculates the precision of a confusion matrix, which is the percentage of
positive predictions that are correct."
  [cls conf-matrix]
  (let [tp (true-positives cls conf-matrix)
        fp (false-positives cls conf-matrix)]
    (if (zero? (+ tp fp))
      0
      (/ tp (+ tp fp)))))

(defn recall
  "Calculates the recall of a confusion matrix, which is the percentage
of positive labeled instances that were predicted as positive."
  [cls conf-matrix]
  (let [tp (true-positives cls conf-matrix)
        fn (false-negatives cls conf-matrix)]
    (if (zero? (+ tp fn))
      0
      (/ tp (+ tp fn)))))

(defn sensitivity
  "Alias for recall."
  [cls conf-matrix]
  (recall cls conf-matrix))

(defn specificity
  "Calculates the specificity of a confusion matrix, which is the percentage of
negative labeled instances that were predicted as negative."
  [cls conf-matrix]
  (let [tn (true-negatives cls conf-matrix)
        fp (false-positives cls conf-matrix)]
    (if (zero? (+ tn fp))
      0
      (/ tn (+ tn fp)))))

(defn accuracy
  "Calculates the accuracy of a confusion matrix, which is the percentage of
predictions that are correct."
  [conf-matrix]
  (let [tp (true-positives conf-matrix)
        tn (true-negatives conf-matrix)
        fp (false-positives conf-matrix)
        fn (false-negatives conf-matrix)]
    (/ (+ tp tn) (+ tp tn fp fn))))

(defn f1-score
  "Calculates the F1 score of a confusion matrix, which is a weighted average
of the precision and recall."
  [cls conf-matrix]
  (let [prec (precision cls conf-matrix)
        rec (recall cls conf-matrix)]
    (if (zero? (+ prec rec))
      0
      (* 2 (/ (*  prec rec) (+ prec rec))))))