(ns judgr.db.memory-db
  (:use [judgr.db.base]))

(deftype MemoryDB [settings items features]
  FeatureDB
  (add-item! [db item class]
    (ensure-valid-class settings class
      (let [data {:item item :class class}]
        (dosync
         (alter items conj data))
        data)))

  (clear-db! [db]
    (dosync
     (ref-set items [])
     (ref-set features [])))

  (add-feature! [db item feature class]
    (ensure-valid-class settings class
      (let [f (.get-feature db feature)]
        (dosync
         (if (nil? f)
           (let [data {:feature feature
                       :total 1
                       :classes {class 1}}]
             (alter features assoc feature data)
             data)
           (let [total-count (or (-> f :total) 0)
                 class-count (or (-> f :classes class) 0)
                 data (assoc-in (assoc f :total (inc total-count))
                                [:classes class] (inc class-count))]
             (alter features assoc feature data)
             data))))))

  (get-feature [db feature]
    (@features feature))

  (count-features [db]
    (count @features))

  (get-items [db]
    @items)

  (count-items [db]
    (count @items))

  (count-items-of-class [db class]
    (count (filter #(= (:class %) class) @items))))