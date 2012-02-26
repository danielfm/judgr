(ns clj-thatfinger.test.settings
  (:use [clj-thatfinger.settings]
        [clojure.test]))

(deftest updating-settings
  (testing "changing an existing setting"
    (let [new-settings (update-settings settings
                                        [:classes :ok :threshold] 3)]
      (is (= 3 (-> new-settings :classes :ok :threshold)))
      (is (= 2.5 (-> new-settings :classes :offensive :threshold)))))

  (testing "adding a new setting"
    (let [new-settings (update-settings settings
                                        [:database :mydb :host] "localhost")]
      (is (= "localhost" (-> new-settings :database :mydb :host)))
      (is (= 27017 (-> new-settings :database :mongo :port))))))