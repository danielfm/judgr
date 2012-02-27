(ns clj-thatfinger.test.settings
  (:use [clj-thatfinger.settings]
        [clojure.test]))

(deftest updating-settings
  (testing "changing an existing setting"
    (let [new-settings (update-settings settings
                                        [:classes] [:ok :not-ok])]
      (is (= [:ok :not-ok] (-> new-settings :classes)))))

  (testing "adding a new setting"
    (let [new-settings (update-settings settings
                                        [:database :mydb :host] "localhost")]
      (is (= "localhost" (-> new-settings :database :mydb :host)))
      (is (= 27017 (-> new-settings :database :mongo :port))))))