(ns judgr.test.settings
  (:use [judgr.settings]
        [clojure.test]))

(deftest updating-settings
  (testing "changing an existing setting"
    (let [new-settings (update-settings settings
                                        [:classes] [:ok :not-ok])]
      (is (= [:ok :not-ok] (-> new-settings :classes)))))

  (testing "adding a new setting"
    (let [new-settings (update-settings settings
                                        [:database :setting] "value")]
      (is (= "value" (-> new-settings :database :setting)))
      (is (= :memory (-> new-settings :database :type))))))