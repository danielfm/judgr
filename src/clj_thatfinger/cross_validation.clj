(ns clj-thatfinger.cross-validation
  (:require [clj-thatfinger.db [memory-db :as memory-db]])
  (:use [clj-thatfinger.core]
        [clj-thatfinger.db.default-db]))

(defn partition-messages
  "Partitions all messages into k chunks. Each chunk is guaranteed to have
at least two messages in it."
  [k]
  (let [count (count-messages)]
   (partition (max (int (/ count (if (zero? k) 1 k))) 2) (get-all-messages))))

(defn remove-nth
  "Remove the nth element of a collection."
  [n coll]
  (concat (take n coll)
          (drop (inc n) coll)))

(defn train-partition!
  "Trains the chunk of messages msgs."
  [msgs]
  (doall (map #(add-message! (:message %) (keyword (:class %))) msgs)))

(defn train-all-partitions-but!
  "Trains all chunks of messages except the chunk at nth position."
  [k msgs]
  (doall (map #(train-partition! %) (remove-nth k msgs))))

(defn expected-predicted-count
  "Returns a map {:expected-class {:predicted-class 1}}."
  [msg]
  {(keyword (:class msg)) {(classify (:message msg)) 1}})

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
  "Evaluates the trained model against the chunk of messages msgs."
  [msgs]
  (reduce add-results (map expected-predicted-count msgs)))

(defn k-fold-cross-validation
  "Performs k-fold cross validation and return a confusion matrix as a map
of maps."
  [k]
  (let [subsets (partition-messages k)]
    (memory-db/with-memory-db
      (reduce add-results (map (fn [i]
                                 (train-all-partitions-but! i subsets)
                                 (eval-model (nth subsets i))) (range (count subsets)))))))