(ns judgr.test.classifier.default-classifier
  (:use [judgr.classifier.default-classifier]
        [judgr.core]
        [judgr.test.util]
        [judgr.settings]
        [clojure.test])
  (:import [judgr.classifier.default_classifier DefaultClassifier]))

(def new-settings
  (update-settings settings
                   [:database :type] :memory
                   [:classifier :type] :default
                   [:extractor :type] :brazilian-text))

(def-fixture empty-db []
  (let [classifier (classifier-from new-settings)]
    (test-body)))

(def-fixture basic-db []
  (let [classifier (classifier-from new-settings)]
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

(def-fixture thresholds [thresholds]
  (let [new-settings (update-settings new-settings
                                      [:classifier :default :threshold?] true
                                      [:classifier :default :thresholds] thresholds)]
    (test-body)))

(def-fixture threshold-enabled? [threshold?]
  (let [new-settings (update-settings new-settings
                                      [:classifier :default :threshold?] threshold?)]
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

(deftest calculating-probability-of-class-given-item
  (with-fixture smoothing-factor [1]
    (with-fixture basic-db []
      (are [cls item v (close-to? v (.probability-of-class-given-item classifier cls item))]
           :offensive "Filha do diabo." 4112702/4656612))))

(deftest calculating-probabilities-of-item
  (with-fixture smoothing-factor [1]
    (with-fixture basic-db []
      (let [probs (.probabilities classifier "Filha do diabo.")]
        (are [cls v] (close-to? v (cls probs))
             :offensive 4112702/4656612
             :ok        11073190/46566128)))))

(deftest classifying-item
  (testing "class with greatest probability passes the threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:offensive 2.5 :ok 1.2}]
        (with-fixture basic-db []
          (are [cls item] (= cls (.classify classifier item))
               :offensive "Filha do diabo.")))))

  (testing "unknown item due to failed threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:offensive 50 :ok 1}]
        (with-fixture basic-db []
          (are [cls item] (= cls (.classify classifier item))
               :unknown "Filha do diabo.")))))

  (testing "class with greatest probability without threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:offensive 50 :ok 1}]
        (with-fixture threshold-enabled? [false]
          (with-fixture basic-db []
            (are [cls item] (= cls (.classify classifier item))
                 :offensive "Filha do diabo.")))))))

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
