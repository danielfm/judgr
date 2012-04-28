(ns judgr.settings)

(def ^:dynamic settings
  {:classes   [:positive :negative]

   :extractor {:type :english-text}

   :database  {:type :memory}

   :classifier {:type :default
                :default {:unbiased? false
                          :smoothing-factor 1
                          :threshold? true
                          :thresholds {:positive 1.2
                                       :negative 2.5}
                          :unknown-class :unknown}}})

(defn update-settings
  "Returns an updated version of map m by applying assoc-in.
Ex: (update-settings settings [:unknown-class] :unknown)"
  [m & kvs]
  (letfn [(f [m [k v]]
            (assoc-in m k v))]
    (reduce f m (partition 2 kvs))))