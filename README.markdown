# Judgr

Judgr (pronounced as judger) is a
[naïve Bayes classifier](http://en.wikipedia.org/wiki/Naive_Bayes_classifier)
library written in Clojure which features multivariate classification,
support for cross validation, and more.

## Features

* Multivariate classification
* Biased and unbiased class probabability
* Configurable [Laplace Smoothing](http://en.wikipedia.org/wiki/Additive_smoothing)
* Configurable threshold validation
* K-fold cross-validation
  * Precision, Recall, Specificity, Accuracy, and F1 score
* Out of the box integration with MongoDB

## Getting Started

Clone the project or add the following dependency to your
_project.clj_ file:

````clojure

[judgr "0.1.0"]
````

### Training The Classifier

The first step is to instantiate the classifier given the current settings:

````clojure

user=> (use '[judgr.core]
            '[judgr.settings])
nil
user=> (def classifier (classifier-from settings))
#'user/classifier
````

Now you can start training the classifier with `(.train! classifier item :class)`:

````clojure

(.train! classifier "How are you?" :ok)
(.train! classifier "I hate your kind, I hope you burn in hell." :offensive)
(.train! classifier ...)
````

The default classifier saves data to a local instance of MongoDB and
are capable of extracting words from the given text using a porter
stemmer. Also, items can be classified as either `:ok` or
`:offensive`. If your problem requires different settings, please take
a look at the _Extending The Classifier_ section below.

### Classifying Items

After some training you should be able to use the classifier to guess
on which class that item falls into:

````clojure

user=> (.classify classifier "Long time, no see.")
:ok
user=> (.classify classifier "Go to hell.")
:offensive
````

It's also possible to get the probabilities for all classes:

````clojure

user=> (.probabilities "Long time, no see.")
{:offensive 0.38461539149284363, :ok 0.6153846383094788}
````

### Evaluating The Classifier

It's not that trivial to measure how well the classifier is
generalizing to examples it doesn't know about. Fortunately, there's a
common technique to evaluate an algorithm's performance that is known
as [Cross-validation](http://en.wikipedia.org/wiki/Cross-validation).

The output of a _K_-Fold Cross-validation process is a
[Confusion Matrix](http://en.wikipedia.org/wiki/Confusion_matrix).

````clojure

user=> (use 'judgr.cross-validation)
nil
user=> (def conf-matrix (k-fold-crossval 2 classifier))
#'user/conf-matrix
user=> conf-matrix
{:ok {:ok 102
      :offensive 3}
 :offensive {:ok 7
             :offensive 186}}
````

This Confusion Matrix will tell, for each known class, how many items
it predicted correctly, and how many items it predicted as being in
another class. For example, for all items known as `:ok`, 102 items
were flagged correctly and 3 were flagged incorrectly as `:offensive`.

Although this helps, it would be nice to have ways to calculate a
single number score.

#### Accuracy

The Accuracy is the percentage of predictions that the classifier got
correct:

````clojure

user=> (accuracy conf-matrix)
144/149
````

In case of low accuracy, there are other calculations that might help
you identify what's wrong.

#### Precision

_Precision_ is a measure of the accuracy provided that a specific
class has been predicted:

````clojure

user=> (precision :ok conf-matrix)
102/109
user=> (precision :offensive conf-matrix)
62/63
````

#### Recall

_Recall_ is a measure of the ability of a model to select instances of
a certain class from a data set. It is commonly also called
_Sensitivity_, and corresponds to the true positive rate:

````clojure

user=> (recall :ok conf-matrix)
34/35
user=> (recall :offensive conf-matrix)
186/193
user=> (sensitivity :offensive conf-matrix)
186/193
````

#### F1 Score

F1 Score is a weighted average of the precision and recall of a given
class:

````clojure

user=> (f1-score :ok conf-matrix)
102/107
user=> (f1-score :offensive conf-matrix)
186/191
````

#### References

1. [Accurary and precision](http://en.wikipedia.org/wiki/Accuracy_and_precision)
2. [Precision and recall](http://en.wikipedia.org/wiki/Precision_and_recall)

## Extending The Classifier

There are several ways to change the way the classifier works.

### Supported Classes

Change the `[:classes]` setting to the classes you want to use. For
example, if you are building a spam classifier:

````clojure

(use 'judgr.settings)

(def my-settings
  (update-settings settings
                   [:classes] [:spam :ham]))
````

### Feature Extraction

#### English And Brazilian Portuguese

We provide simple implementations for English (default) and Brazilian
Portuguese, based on the work done in
[Apache Lucene](http://lucene.apache.org/core/).

#### Providing Your Own Feature Extractor

The first thing you have to do is create a type that extends the
`FeatureExtractor` protocol:

````clojure

(ns your-ns
  (:use [judgr.extractor.base]))

(deftype CustomExtractor [settings]
  FeatureExtractor

  (extract-features [fe item]
    ;; Feature extraction logic here
    ))
````

Finally, define a new method for `extractor-from` multimethod that
knows how to create a new instance of `CustomExtractor`:

````clojure

(ns your-ns
  (:use [judgr.core]))

(defmethod extractor-from :custom [settings]
  (CustomExtractor. settings))
````

To use the new extractor, just create a new settings map with
`[:extractor :type]` setting configured to `:custom`, the same key
used in `defmethod`:

````clojure

user=> (use 'judgr.settings)
nil
user=> (def my-settings
         (update-settings settings
                          [:extractor :type] :custom))
#'user/my-settings
user=> (extractor-from my-settings)
#<CustomExtractor ...>
````

### Database Integration

#### MongoDB

MongoDB integration is enabled by default.

Change the `[:database :mongo *]` settings if the default values don't
apply for you:

````clojure

(use 'judgr.settings)

(def my-settings
  (update-settings settings
                   [:database :mongo :host] "your-mongodb-host"
                   [:database :mongo :auth?] true
                   ;; ...
                   ))
````

#### Providing Your Own Database Layer

The procedure is similar to what was shown in _Providing Your Own
Feature Extractor_ section.

First, create a new type that extends the `FeatureDB` protocol:

````clojure

(ns your-ns
  (:use [judgr.db.base]))

(deftype CustomDB [settings]
  FeatureDB

  (add-item! [db item class]
    ;; ...
    )

  ;; Implement the other methods
)
````

Then, define a new method for `db-from` multimethod that knows how to
create a new instance of `CustomDB`:

````clojure

(ns your-ns
  (:use [judgr.core]))

(defmethod db-from :custom [settings]
  (CustomDB. settings))
````

To use the new database layer, just create a new settings map with
`[:database :type]` setting configured to `:custom`, the same key used
in `defmethod`:

````clojure

user=> (use 'judgr.settings)
nil
user=> (def my-settings
         (update-settings settings
                          [:database :type] :custom))
#'user/settings
user=> (db-from my-settings)
#<CustomDB ...>
````

### Classifier Implementation

#### Default Classifier

There's a default classifier implementation that should be enough for
most cases since it is already fairly configurable.

##### Threshold Validation

If threshold validation is enabled,
i.e. `[:classifier :default :threshold?]` setting is `true`, an item
will only be flagged as a class if its probability is at least _X_
times greater than the second highest probability. The threshold for
each class can be configured in `[:classifier :default :thresholds]`
setting:

````clojure

(use 'judgr.settings)

(def my-settings
  (update-settings settings
                   [:classifier :default :threshold?] true
                   [:classifier :default :thresholds] {:ok 1 :offensive 2}))
````

If the probabilities for an item are `{:ok 0.45 :offensive 0.55}`, and
their thesholds are 1 and 2, respectively, the item will be flagged
with the value defined in `[:classifier :default :unknown-class]`
setting, which is `:unknown` by default.

##### Smoothing

Smoothing is enabled by default, and it's useful to deal with unknown
features by not returning a flat zero probability.

You can change the `[:classifier :default :smoothing-factor]` setting
to change the smoothing intensity, although the default value is
usually good enough:

````clojure

(use 'judgr.settings)

(def my-settings
  (update-settings settings
                   [:classifier :default :smoothing-factor] 0.7))
````

Although it's not recommended, you can turn smoothing off by changing
the `[:classifier :default :smoothing-factor]` setting to zero.

##### Using Unbiased Class Probabilities

By default, class probabilities are calculated in a _biased_ fashion,
that is, considering the number of items flagged in each class. For
example, considering smoothing is disabled, if there's no item flagged
as `:offensive`, the probability _P(offensive) = 0_. Similarly, if
there's 3 offensive items out of 10, then _P(offensive) = 3/10_.

If the `[:classifier :default :unbiased?]` setting is configured to
`true`, the probability _P(any_class) = 1/(number_of_classes)_:

````clojure

(use 'judgr.settings)

(def my-settings
  (update-settings settings
                   [:classifier :default :unbiased?] true))
```

### Providing Your Own Classifier

First, create a new type that extends the `Classifier` protocol:

````clojure

(ns your-ns
  (:use [judgr.classifier.base]))

(deftype CustomClassifier [settings db extractor]
  Classifier

  (train! [c item class]
    ;; ...
    )

  ;; Implement the other methods
)
````

Then, define a new method for `classifier-from` multimethod that knows
how to create a new instance of `CustomClassifier`:

````clojure

(ns your-ns
  (:use [judgr.core]))

(defmethod classifier-from :custom [settings]
  (let [db (db-from settings)
        extractor (extractor-from settings)]
    (CustomClassifier. settings db extractor)))
````

To use the new classifier, just create a new settings map with
`[:classifier :type]` setting configured to `:custom`, the same key
used in `defmethod`:

````clojure

user=> (use 'judgr.settings)
nil
user=> (def my-settings
         (update-settings settings
                          [:classifier :type] :custom))
#'user/settings
user=> (classifier-from settings)
#<CustomClassifier ...>
````

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.
