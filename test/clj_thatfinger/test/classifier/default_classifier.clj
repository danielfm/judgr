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
    (doall (map (fn [[item class]] (.train! classifier item class))
                '(["Você é um diabo, mesmo." :ok]
                  ["Sai de ré, capeta." :offensive]
                  ["Vai pro inferno, diabo!" :offensive]
                  ["Sua filha é uma diaba, doido." :offensive])))
    (test-body)))

(def-fixture smoothing-factor [factor]
  (let [new-settings (update-settings new-settings
                       [:classifier :default :smoothing-factor] factor)]
    (test-body)))

(def-fixture unbiased? [unbiased]
  (let [new-settings (update-settings new-settings
                       [:classifier :default :unbiased?] unbiased)]
    (test-body)))

(deftest ensure-mongodb
  (with-fixture empty-db []
    (is (instance? DefaultClassifier classifier))))

(deftest calculating-probabilities
  (testing "with smoothing enabled"
    (with-fixture smoothing-factor [1]
      (with-fixture empty-db []
        (are [cls total v] (close-to? v (probability cls total 2 new-settings))
             3   100 4/102
             0   100 1/102
             nil 100 1/102))))

  (testing "with smoothing disabled"
    (with-fixture smoothing-factor [0]
      (with-fixture empty-db []
        (are [cls total v] (close-to? v (probability cls total 2 new-settings))
             3   100 3/100
             0   100 0
             nil 100 0)))))

(deftest calculating-probability-of-class
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :ok        1/3
             :offensive 2/3))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :ok        1/4
             :offensive 3/4))))

  (testing "unbiased probability"
    (with-fixture unbiased? [true]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :ok        1/2
             :offensive 1/2)))))

(deftest calculating-probability-of-feature-given-class
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [f cls v] (close-to? v (.feature-probability-given-class classifier f cls))
             "diab" :ok        1/6
             "diab" :offensive 3/14
             "else" :ok        1/12))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [f cls v] (close-to? v (.feature-probability-given-class classifier f cls))
             "diab" :ok        1
             "diab" :offensive 2/3
             "else" :ok        0)))))

(deftest calculating-probability-of-class-given-feature
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [cls f v] (close-to? v (.class-probability-given-feature classifier cls f))
             :ok        "diab" 7/25
             :offensive "diab" 18/25))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [cls f v] (close-to? v (.class-probability-given-feature classifier cls f))
             :ok        "diab" 1/3
             :offensive "diab" 2/3)))))

(deftest training
  (with-fixture empty-db []
    (.train! classifier "Sai de ré, capeta." :offensive)

    (testing "should add item"
      (let [item (last (.get-items (.db classifier)))]
        (is (= 1 (.count-items (.db classifier))))
        (are [k v] (= v (k item))
             :item  "Sai de ré, capeta."
             :class :offensive)))

    (testing "should add item's features"
      (is (= 3 (.count-features (.db classifier))))
      (are [feature] (not (nil? (.get-feature (.db classifier) feature)))
           "sai"
           "ré"
           "capet"))))