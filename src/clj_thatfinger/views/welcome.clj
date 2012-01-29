(ns clj-thatfinger.views.welcome
  (:require [clj-thatfinger.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to ae"]))
