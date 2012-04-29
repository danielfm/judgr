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

(def-fixture classes [classes]
  (let [new-settings (update-settings new-settings
                                      [:classes] classes)]
    (test-body)))

(def-fixture basic-db []
  (let [classifier (classifier-from new-settings)]
    (doall (map (fn [[item class]] (.train! classifier item class))
                '(["Você é um diabo, mesmo." :positive]
                  ["Sai de ré, capeta." :negative]
                  ["Vai pro inferno, diabo!" :negative]
                  ["Sua filha é uma diaba, doido." :negative])))
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

(deftest ensure-default-classifier
  (with-fixture empty-db []
    (is (instance? DefaultClassifier classifier))))

(deftest calculating-probabilities
  (testing "with smoothing enabled"
    (with-fixture smoothing-factor [1]
      (with-fixture empty-db []
        (are [cls total v] (close-to? v (probability cls total 2 new-settings))
             3   100 4/102
             0   100 1/102
             0   0   1/2
             nil 100 1/102))))

  (testing "with smoothing disabled"
    (with-fixture smoothing-factor [0]
      (with-fixture empty-db []
        (are [cls total v] (close-to? v (probability cls total 2 new-settings))
             3   100 3/100
             0   100 0
             nil 100 0)

        (testing "without training data"
          (is (thrown-with-msg? IllegalStateException #"no training data"
                (probability 10 0 2 new-settings))))))))

(deftest calculating-probability-of-class
  (testing "with no configured classes"
    (with-fixture classes [{}]
      (with-fixture empty-db []
        (is (thrown-with-msg? IllegalStateException
              #"specify \[:classes\] setting"
              (.class-probability classifier :positive))))))

  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :positive 1/3
             :negative 2/3))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :positive 1/4
             :negative 3/4))))

  (testing "unbiased probability"
    (with-fixture unbiased? [true]
      (with-fixture basic-db []
        (are [cls v] (close-to? v (.class-probability classifier cls))
             :positive 1/2
             :negative 1/2)))))

(deftest calculating-probability-of-feature-given-class
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [f cls v] (close-to? v (.feature-probability-given-class classifier f cls))
             "diab" :positive 1/6
             "diab" :negative 3/14
             "filh" :positive 1/12
             "filh" :negative 1/7
             "else" :positive 1/12))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [f cls v] (close-to? v (.feature-probability-given-class classifier f cls))
             "diab" :positive 1
             "diab" :negative 2/3
             "filh" :positive 0
             "filh" :negative 1/3
             "else" :positive 0)))))

(deftest calculating-probability-of-feature
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (are [f v] (close-to? v (.feature-probability classifier f))
             "diab" 4/15
             "filh" 2/15
             "else" 1/15))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (are [f v] (close-to? v (.feature-probability classifier f))
             "diab" 3/4
             "filh" 1/4
             "else" 0)))))

(deftest calculating-probabilities-of-item
  (testing "with smoothing"
    (with-fixture smoothing-factor [1]
      (with-fixture basic-db []
        (let [probs (.probabilities classifier "Filha do diabo.")]
          (are [cls v] (close-to? v (cls probs))
               :negative 225/392
               :positive 25/192)))))

  (testing "without smoothing"
    (with-fixture smoothing-factor [0]
      (with-fixture basic-db []
        (let [probs (.probabilities classifier "Filha do diabo.")]
          (are [cls v] (close-to? v (cls probs))
               :negative 8/9
               :positive 0))))))

(deftest classifying-item
  (testing "threshold test enabled, but threshold for class is not specified"
    (with-fixture smoothing-factor [0]
      (with-fixture thresholds [{:offensive 2.5 :ok 1.2}]
        (with-fixture basic-db []
          (is (thrown-with-msg?
                IllegalArgumentException
                #"specify \[:classifier :default :thresholds\] setting"
                (.classify classifier "Filha do diabo.")))))))

  (testing "class with greatest probability passes the threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:negative 2.5 :positive 1.2}]
        (with-fixture basic-db []
          (are [cls item] (= cls (.classify classifier item))
               :negative "Filha do diabo.")))))

  (testing "unknown item due to failed threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:negative 50 :positive 1}]
        (with-fixture basic-db []
          (are [cls item] (= cls (.classify classifier item))
               :unknown "Filha do diabo.")))))

  (testing "class with greatest probability without threshold test"
    (with-fixture smoothing-factor [1]
      (with-fixture thresholds [{:negative 50 :positive 1}]
        (with-fixture threshold-enabled? [false]
          (with-fixture basic-db []
            (are [cls item] (= cls (.classify classifier item))
                 :negative "Filha do diabo.")))))))

(deftest training
  (with-fixture empty-db []
    (.train! classifier "Sai de ré, capeta." :negative)

    (testing "should add item"
      (let [item (last (.get-items (.db classifier)))]
        (is (= 1 (.count-items (.db classifier))))
        (are [k v] (= v (k item))
             :item  "Sai de ré, capeta."
             :class :negative)))

    (testing "should add item's features"
      (is (= 3 (.count-features (.db classifier))))
      (are [feature] (not (nil? (.get-feature (.db classifier) feature)))
           "sai"
           "ré"
           "capet"))))
