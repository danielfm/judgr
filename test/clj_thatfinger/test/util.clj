(ns clj-thatfinger.test.util
  (:use [clojure.tools.macro]))

(def ^:dynamic *fixtures* (atom {}))

(defmacro def-fixture [name args & body]
  `(swap! *fixtures* assoc '~name (cons '~args '~body)))

(defmacro with-fixture [name args & body]
  (let [[largs lbody] (get @*fixtures* name)]
    `(symbol-macrolet [~'test-body (fn [] ~@body)]
                  ((fn ~largs ~lbody)
                   ~@args))))