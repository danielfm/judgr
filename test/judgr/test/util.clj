(ns judgr.test.util
  (:use [clojure.tools.macro]))

(def ^:dynamic *fixtures* (atom {}))

(defmacro def-fixture [name args & body]
  `(swap! *fixtures* assoc '~name (cons '~args '~body)))

(defmacro with-fixture [name args & body]
  (let [[largs lbody] (get @*fixtures* name)]
    `(symbol-macrolet [~'test-body (fn [] ~@body)]
                  ((fn ~largs ~lbody)
                   ~@args))))

(defn close-to?
  "Returns whether the two given numbers are close enough."
  [f1 f2]
  (let [prec 0.001]
    (<= (Math/abs (- (float f2) (float f1))) prec)))