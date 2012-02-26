(ns clj-thatfinger.test.classifier.default-classifier
  (:use [clj-thatfinger.classifier.default-classifier]
        [clj-thatfinger.factory]
        [clj-thatfinger.test.util]
        [clj-thatfinger.settings]
        [clojure.test])
  (:import [clj_thatfinger.classifier.default_classifier DefaultClassifier]))

(def new-settings
  (update-settings settings
                   [:database :type] :memory
                   [:classifier :type] :default
                   [:extractor :type] :brazilian-text))

(def-fixture empty-db []
  (let [classifier (use-classifier new-settings)]
    (test-body)))

(def-fixture basic-db []
  (let [classifier (use-classifier new-settings)]
    (doall (map (fn [[item class]]
                  (.train! classifier item class))
                '(["Você é um diabo, mesmo." :ok]
                  ["Sai de ré, capeta." :offensive]
                  ["Vai pro inferno, diabo!" :offensive]
                  ["Sua filha é uma diaba, doido." :offensive])))

    (test-body)))

(deftest ensure-mongodb
  (with-fixture empty-db []
    (is (instance? DefaultClassifier classifier))))

(deftest training
  (with-fixture empty-db []
    (.train! classifier "Sai de ré, capeta." :offensive)

    (testing "should add item"
      (let [db (.db classifier)
            item (last (.get-items db))]
        (is (= "Sai de ré, capeta." (:item item)))
        (is (= :offensive (:class item)))))

    (testing "should add item's features"
      (let [db (.db classifier)]
        (is (= 3 (.count-features db)))
        (are [feature] (not (nil? (.get-feature db feature)))
             "sai" "ré" "capet")))))