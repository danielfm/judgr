(ns clj-thatfinger.db.memory-db
  (:use [clj-thatfinger.db.base])
  (:import [java.util Date]))

(deftype MemoryDB [settings items-atom features-atom]
  FeatureDB
  (add-item! [db item class]
    (ensure-valid-class settings class
      (let [data {:item item
                  :created-at (Date.)
                  :class class}]
        (swap! items-atom conj data)
        data)))

  (add-feature! [db item feature class]
    (ensure-valid-class settings class
      (let [f (.get-feature db feature)]
        (if (nil? f)
          (let [data {:feature feature
                      :total 1
                      :classes {class 1}}]
            (swap! features-atom assoc feature data)
            data)
          (let [total-count (or (-> f :total) 0)
                class-count (or (-> f :classes class) 0)
                data (assoc-in (assoc f :total (inc total-count))
                               [:classes class] (inc class-count))]
            (swap! features-atom assoc feature data)
            data)))))

  (get-feature [db feature]
    (@features-atom feature))

  (count-features [db]
    (count @features-atom))

  (get-items [db]
    @items-atom)

  (count-items [db]
    (count @items-atom))

  (count-items-of-class [db class]
    (count (filter #(= (:class %) class) @items-atom))))