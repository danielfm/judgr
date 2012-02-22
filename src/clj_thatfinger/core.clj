(ns clj-thatfinger.core
  (:use [clj-thatfinger.probability]
        [clj-thatfinger.db.default-db]
        [clj-thatfinger.stemmer.default-stemmer]
        [clj-thatfinger.settings]))

(defn prob-of-class
  "Returns the weighted probability of an item to be part of class."
  [class]
  (let [class-count (count *classes*)]
    (if *classes-unbiased*
      (prob 1 class-count 0)
      (prob (count-items-of class) (count-items) class-count))))

(defn prob-of-feature
  "Returns the probability of a feature given the item is classified as class."
  [feature class]
  (let [f (get-feature feature)]
    (prob (-> f :classes class) (count-items-of class) (count-features))))

(defn posterior-prob-of-feature
  "Returns the probability of class given feature is present."
  [class feature]
  (let [prior (* (prob-of-feature feature class) (prob-of-class class))
        total-prob (reduce + (map #(* (prob-of-feature feature %)
                                      (prob-of-class %)) (keys *classes*)))]
    (if (zero? total-prob)
      0
      (float (/ prior total-prob)))))

(defn posterior-prob-of-item
  "Returns the probability that item is classified as class."
  [item class]
  (let [items (stem item)]
    (fisher-prob (map #(posterior-prob-of-feature class %) items))))

(defn posterior-probs
  "Returns the probabilities of item for each possible class."
  [item]
  (let [classes (keys *classes*)]
    (zipmap classes (map #(posterior-prob-of-item item %) classes))))

(defn train!
  "Labels a item with the given class."
  [item class]
  (add-item! item class))

(defn classify
  "Returns the class with the highest probability for item that passes the
threshold validation."
  [item]
  (let [posterior-probs (reverse (sort-by val (posterior-probs item)))
        [first-prob second-prob & _]  posterior-probs]
    (if (or (not *threshold-enabled*)
            (<= (* (val second-prob)
                   (-> ((key first-prob) *classes*) :threshold))
                (val first-prob)))
      (key first-prob)
      *class-unknown*)))