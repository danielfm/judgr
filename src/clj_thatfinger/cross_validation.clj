(ns clj-thatfinger.cross-validation
  (:require [clj-thatfinger.db.memory-db :as memory-db]
            [clj-thatfinger.db.default-db :as default-db])
  (:use [clj-thatfinger.core]))

(defn partition-items
  "Partitions all items into k chunks. Each chunk is guaranteed to have
at least two items in it."
  [k]
  (let [count (default-db/count-items)]
   (partition (max (int (/ count (if (zero? k) 1 k))) 2) (default-db/get-items))))

(defn remove-nth
  "Remove the nth element of a collection."
  [n coll]
  (concat (take n coll)
          (drop (inc n) coll)))

(defn train-partition!
  "Trains the chunk of items."
  [items]
  (doall (map #(train! (:item %) (keyword (:class %))) items)))

(defn train-all-partitions-but!
  "Trains all chunks of items except the chunk at nth position."
  [k items]
  (doall (map #(train-partition! %) (remove-nth k items))))

(defn expected-predicted-count
  "Returns a map {:expected-class {:predicted-class 1}}."
  [item]
  {(keyword (:class item)) {(classify (:item item)) 1}})

(defn nested-dissoc
  "Dissoc a nested property defined by path from map."
  [map path]
  (let [parent-path (drop-last path)
        parent (get-in map parent-path)]
    (if (empty? parent-path)
      (dissoc map (last path))
      (assoc-in map parent-path (dissoc parent (last path))))))

(defn add-results
  "Adds two expected-predicted-count maps together."
  ([r1 r2]
     (add-results r1 r2 []))
  ([r1 r2 path]
     (if (empty? r2)
       r1
       (let [cur-val (get-in r2 path)]
         (cond
          (number? cur-val) (recur (assoc-in r1 path (+ (or (get-in r1 path) 0) cur-val))
                                   (nested-dissoc r2 path)
                                   (vec (drop-last path)))
          (and (empty? cur-val)) (recur r1 (nested-dissoc r2 path) (vec (drop-last path)))
          :else (recur r1 r2 (conj path (first (keys cur-val)))))))))

(defn eval-model
  "Evaluates the trained model against the chunk of items."
  [items]
  (reduce add-results (map expected-predicted-count items)))

(defn k-fold-crossval
  "Performs k-fold cross validation and return a confusion matrix as a map
of maps, where the first level contains the expected classes and the second
level contains predicted classes."
  [k]
  (let [subsets (partition-items k)]
    (memory-db/with-memory-db
      (let [lombra (map (fn [i]
                          (train-all-partitions-but! i subsets)
                          (eval-model (nth subsets i)))
                        (range (count subsets)))]
        (reduce add-results lombra)))))

(defn- apply-to-each-key
  "Calls (f key map) for each key of map m and returns a hash-map with the
result for each key."
  [f m]
  (apply hash-map (flatten (map #(list % (f % m)) (keys m)))))

(defn true-positives
  "Returns the number of true positives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key true-positives conf-matrix))))
  ([cls conf-matrix]
     (-> conf-matrix cls cls)))

(defn false-positives
  "Returns the number of false positives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key false-positives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals (apply-to-each-key #(get-in %2 [% cls]) (dissoc conf-matrix cls))))))

(defn false-negatives
  "Returns the number of false negatives of a class or the entire confusion
matrix."
  ([conf-matrix]
     (reduce + (vals (apply-to-each-key false-negatives conf-matrix))))
  ([cls conf-matrix]
     (reduce + (vals (dissoc (apply-to-each-key #(get-in %2 [cls %])
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
    (float (/ tp (+ tp fp)))))

(defn recall
  "Calculates the recall of a confusion matrix, which is the percentage of positive
labeled instances that were predicted as positive."
  [cls conf-matrix]
  (let [tp (true-positives cls conf-matrix)
        fn (false-negatives cls conf-matrix)]
    (float (/ tp (+ tp fn)))))

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
    (float (/ tn (+ tn fp)))))

(defn accuracy
  "Calculates the accuracy of a confusion matrix, which is the percentage of
predictions that are correct."
  [conf-matrix]
  (let [tp (true-positives conf-matrix)
        tn (true-negatives conf-matrix)
        fp (false-positives conf-matrix)
        fn (false-negatives conf-matrix)]
    (float (/ (+ tp tn) (+ tp tn fp fn)))))

(defn f1-score
  "Calculates the F1 score of a confusion matrix, which is a weighted average
of the precision and recall."
  [cls conf-matrix]
  (let [prec (precision cls conf-matrix)
        rec (recall cls conf-matrix)]
    (float (* 2 (/ (*  prec rec) (+ prec rec))))))