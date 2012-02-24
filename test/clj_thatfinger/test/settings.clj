(ns clj-thatfinger.test.settings)

(def settings {:classes {:ok {:threshold 1}
                         :offensive {:threshold 2}}
               :mongodb {:host "localhost"
                         :port 27017
                         :database "clj-thatfinger-test"
                         :auth? false
                         :username ""
                         :password ""}})