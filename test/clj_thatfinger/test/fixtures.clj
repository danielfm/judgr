(ns clj-thatfinger.test.fixtures
  (:use [clj-thatfinger.db.default-db]
        [clj-thatfinger.test.utils]
        [clojure.tools.macro]))

(def ^:dynamic *fixtures* (atom {}))

(defmacro def-fixture [name args & body]
  `(swap! *fixtures* assoc '~name (cons '~args '~body)))

(defmacro with-fixture [name args & body]
  (let [[largs lbody] (get @*fixtures* name)]
    `(symbol-macrolet [~'test-body (fn [] ~@body)]
                  ((fn ~largs ~lbody)
                   ~@args))))

(def-fixture smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* true
            clj-thatfinger.settings/*smoothing-factor* 1]
    (test-body)))

(def-fixture no-smoothing []
  (binding [clj-thatfinger.settings/*smoothing-enabled* false]
    (test-body)))

(def-fixture threshold [classes]
  (binding [clj-thatfinger.settings/*threshold-enabled* true
            clj-thatfinger.settings/*class-unknown* :unknown
            clj-thatfinger.settings/*classes* classes]
    (test-body)))

(def-fixture without-threshold []
  (binding [clj-thatfinger.settings/*threshold-enabled* false]
    (test-body)))

(def-fixture test-db []
  (binding [clj-thatfinger.db.memory-db/*words* (atom {})
            clj-thatfinger.db.memory-db/*messages* (atom {})]
    (let [messages [["Você é um diabo, mesmo." :ok]
                    ["Sai de ré, capeta." :offensive]
                    ["Vai pro inferno, diabo!" :offensive]
                    ["Sua filha é uma diaba, doido." :offensive]]]
      (doall (map #(apply train! %) messages)))
    (test-body)))