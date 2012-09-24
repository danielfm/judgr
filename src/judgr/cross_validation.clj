(ns judgr.cross-validation
  (:use [judgr.core]
        [judgr.collection-util]
        [judgr.settings]))

(defn- in-memory-classifier
  "Returns a classifier based on the given one that uses an in-memory database."
  [classifier]
  (let [settings (update-settings (.settings classifier)
                                  [:database :type] :memory)]
    (classifier-from settings)))

(defn partition-items
  "Partitions all items into k chunks. Each chunk is guaranteed to have
at least two items in it."
  [k db]
  (let [count (.count-items db)
        size (max (int (/ count (if (zero? k) 1 k))) 2)]
    (partition size (.get-items db))))

(defn train-all-partitions-but!
  "Trains all chunks of items except the chunk at nth position."
  [k items classifier]
  (doall (map #(.train-all! classifier %) (remove-nth k items))))

(defn expected-predicted-count
  "Returns a map {:expected-class {:predicted-class 1}}."
  [item classifier]
  {(keyword (:class item)) {(.classify classifier (:item item)) 1}})

(defn eval-model
  "Evaluates the trained model against the chunk of items."
  [items classifier]
  (reduce aggregate-results
          (map #(expected-predicted-count % classifier) items)))

(defn k-fold-crossval
  "Performs k-fold cross validation and return a confusion matrix as a map
of maps, where the first level contains the expected classes and the second
level contains predicted classes."
  [k classifier]
  (let [mem-classifier (in-memory-classifier classifier)
        subsets (partition-items k (.db classifier))]
    (let [results (pmap (fn [i]
                         (train-all-partitions-but! i subsets mem-classifier)
                         (eval-model (nth subsets i) mem-classifier))
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
     (reduce + (vals (apply-to-each-key #(get-in %2 [% cls] 0) (dissoc conf-matrix cls))))))

(defn false-negatives
  "Returns the number of false negatives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key false-negatives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals (dissoc (apply-to-each-key #(get-in %2 [cls %] 0)
                                                (nested-dissoc conf-matrix [cls cls]))
                             cls)))))

(defn true-negatives
  "Returns the number of true negatives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key true-negatives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals (apply concat (map #(dissoc (val %) cls) (dissoc conf-matrix cls)))))))

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