(ns judgr.collection-util)

(defn remove-nth
  "Remove the nth element of a collection."
  [n coll]
  (concat (take n coll)
          (drop (inc n) coll)))

(defn nested-dissoc
  "Dissoc a nested property defined by path from map."
  [map path]
  (let [parent-path (drop-last path)
        parent (get-in map parent-path)]
    (if (empty? parent-path)
      (dissoc map (last path))
      (assoc-in map parent-path (dissoc parent (last path))))))

(defn aggregate-results
  "Adds two expected-predicted-count maps together."
  ([r1 r2]
     (aggregate-results r1 r2 []))
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

(defn apply-to-each-key
  "Calls (f key map) for each key of map m and returns a hash-map with the
result for each key."
  [f m]
  (apply hash-map (flatten (map #(list % (f % m)) (keys m)))))