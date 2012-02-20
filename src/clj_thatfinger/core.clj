(ns clj-thatfinger.core
  (:use [clj-thatfinger.probability]
        [clj-thatfinger.db.default-db]
        [clj-thatfinger.stemmer.default-stemmer]
        [clj-thatfinger.settings]))

(defn prob-of-class
  "Returns the weighted probability of a message to be part of class cls."
  ([cls]
     (prob-of-class cls *default-subset*))
  ([cls subset]
     (let [cls-count (count *classes*)]
       (if *classes-unbiased*
         (prob 1 cls-count cls-count)
         (prob (count-messages cls subset) (count-messages subset) cls-count)))))

(defn prob-of-word
  "Returns the probability of a word given the message is classified as cls."
  ([word cls]
     (prob-of-word word cls *default-subset*))
  ([word cls subset]
     (let [w (get-word word)]
       (prob (-> w subset :classes cls) (count-messages cls subset) (count-words subset)))))

(defn posterior-prob-of-word
  "Returns the probability of cls given word is present."
  ([cls word]
     (posterior-prob-of-word cls word *default-subset*))
  ([cls word subset]
     (let [prior (* (prob-of-word word cls subset) (prob-of-class cls subset))
           total-prob (reduce + (map #(* (prob-of-word word % subset)
                                         (prob-of-class % subset)) (keys *classes*)))]
       (if (zero? total-prob)
         0
         (float (/ prior total-prob))))))

(defn posterior-prob-of-message
  "Returns the probability that message is classified as class cls."
  ([message cls]
     (posterior-prob-of-message message cls *default-subset*))
  ([message cls subset]
     (let [words (stem message)]
       (fisher-prob (map #(posterior-prob-of-word cls % subset) words)))))

(defn posterior-probs
  "Returns the probabilities of message for each possible class."
  ([message]
     (posterior-probs message *default-subset*))
  ([message subset]
     (let [classes (keys *classes*)]
       (zipmap classes (map #(posterior-prob-of-message message % subset) classes)))))



(defn train!
  "Labels a message with the given class."
  ([message cls]
     (train! message cls *default-subset*))
  ([message cls subset]
     (add-message! message cls subset)))

(defn classify
  "Returns the class with the highest probability for message that passes the
threshold validation."
  ([message]
     (classify message *default-subset*))
  ([message subset]
     (let [posterior-probs (reverse (sort-by val (posterior-probs message subset)))
           [first-prob second-prob & _]  posterior-probs]
       (if (or (not *threshold-enabled*)
               (<= (* (val second-prob)
                      (-> ((key first-prob) *classes*) :threshold))
                   (val first-prob)))
         (key first-prob)
         *class-unknown*))))