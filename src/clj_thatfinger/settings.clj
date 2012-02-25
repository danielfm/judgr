(ns clj-thatfinger.settings)

(def ^:dynamic settings
  {:unknown-class :unknown

   :classes   {:ok {:threshold 1.2}
               :offensive {:threshold 2.5}}

   :extractor {:type :brazilian-text-extractor}

   :database  {:type :mongo-db
               :mongo-db  {:host "localhost"
                           :port 27017
                           :database "clj-thatfinger"
                           :auth? false
                           :username ""
                           :password ""}}

   :classifier {:type :default
                :default {:threshold? true
                          :unbiased? false
                          :smoothing-factor 1}}})

(defn update-settings
  "Returns an updated version of map m by applying assoc-in.
Ex: (update-settings settings [:unknown-class] :unknown)"
  [m & kvs]
  (letfn [(f [m [k v]]
            (assoc-in m k v))]
    (reduce f m (partition 2 kvs))))