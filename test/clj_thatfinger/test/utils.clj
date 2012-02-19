(ns clj-thatfinger.test.utils)

(defn float= [f1 f2]
  (let [prec 0.001]
    (<= (Math/abs (- (float f2) (float f1))) prec)))