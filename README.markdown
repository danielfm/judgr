# clj-thatfinger

clj-thatfinger is a multi-class [naïve Bayes classifier](http://en.wikipedia.org/wiki/Naive_Bayes_classifier) written in Clojure.

This project is still in early stage of development, so use it at your own risk.

## Requirements

* [Clojure](http://clojure.org) 1.3
* [Leiningen](http://github.com/technomancy/leiningen) 1.6
* [MongoDB](http://mongodb.org) 2.0+

## Features

* Multiclass classification
* Biased and unbiased class probabability
* Configurable [Laplace Smoothing](http://en.wikipedia.org/wiki/Additive_smoothing)
* Configurable threshold validation
* K-fold cross-validation
  * Precision, Recall, Specificity, Accuracy, and F1 score
* MongoDB integration

## Getting Started

Clone the project and start a Clojure REPL session with `lein repl`:

    user=> (use 'clj-thatfinger.core)

### Training The Classifier

Now you can start training the classifier with `(train! "text" :class)`:

    user=> (train! "How are you?" :ok)
    user=> (train! "I hate your kind, I hope you burn in hell." :offensive)
    user=> (train! ...)

If your problem requires different classes, please take a look at the _Extending The Application_ section.

### Classifying Items

After some training you should be able to use the classifier to guess on which class that item falls into:

    user=> (classify "Long time, no see.")
    :ok
    user=> (classify "Go to hell.")
    :offensive

It's also possible to get the probabilities for all classes:

    user=> (probs "Long time, no see.")
    {:offensive 0.38461539149284363, :ok 0.6153846383094788}

### Evaluating The Classifier

It's not that trivial to measure how the classifier is generalizing to examples it doesn't know about. Fortunately, there's a common technique to evaluate an algorithm's performance that is known as [Cross-validation](http://en.wikipedia.org/wiki/Cross-validation).

The output of a _K_-Fold Cross-validation process is a [Confusion Matrix](http://en.wikipedia.org/wiki/Confusion_matrix):

    user=> (use 'clj-thatfinger.cross-validation)
    user=> (k-fold-crossval 2)
    {:ok {:ok 102
          :offensive 3}
     :offensive {:ok 7
                 :offensive 186}}

This Confusion Matrix will tell, for each known class, how many items it predicted correctly, and how many items it predicted as being in another class. For example, for all items known as `:ok`, 102 items were flagged correctly and 3 were flagged incorrectly as `:offensive`.

Although this helps, it would be nice to have ways to calculate a single number score.

#### Accuracy

The Accuracy is the percentage of predictions that the classifier got correct:

    user=> (accuracy conf-matrix)
    0.96644294

#### Precision

_Precision_ is a measure of the accuracy provided that a specific class has been predicted:

    user=> (precision :ok conf-matrix)
    0.9357798
    user=> (precision :offensive conf-matrix)
    0.984127

#### Recall

_Recall_ is a measure of the ability of a model to select instances of a certain class from a data set. It is commonly also called _Sensitivity_, and corresponds to the true positive rate:

    user=> (recall :ok conf-matrix)
    0.9714286
    user=> (recall :offensive conf-matrix)
    0.9637306
    user=> (sensitivity :offensive conf-matrix)
    0.9637306

#### F1 Score

F1 Score is a weighted average of the precision and recall of a given class:

    user=> (f1-score :ok conf-matrix)
    0.95327103
    user=> (f1-score :offensive conf-matrix)
    0.973822

#### References

1. [Accurary and precision](http://en.wikipedia.org/wiki/Accuracy_and_precision)
2. [Precision and recall](http://en.wikipedia.org/wiki/Precision_and_recall)

## Extending The Application

The application settings can be found at `src/clj_thatfinger/settings.clj`.

### Supported Classes

Change the `*classes*` var to the classes you want to use along with their thresholds. By default, the classes supported are `:ok` and `:offensive`.

#### Threshold Validation

If threshold validation is enabled, i.e. `*threshold-enabled*` is true, an item will only be flagged as a class if its probability is at least _X_ times greater than the second highest probability.

For example, if the probabilities for an item are `{:ok 0.45 :offensive 0.55}`, and their thesholds are 1 and 2, respectively, the item will be flagged with the value defined in `*class-unknown*` var, which is `:unknown` by default.

#### Using Unbiased Class Probabilities

By default, class probabilities are calculated in a _biased_ fashion, that is, considering the number of items flagged in each class. For example, considering smoothing is disabled, if there's no item flagged as `:offensive`, the probability _P(offensive) = 0_. Similarly, if there's 3 offensive items out of 10, then _P(offensive) = 3/10_.

If the `*classes-unbiased*` var is set to true, the probability _P(any_class) = 1/(number_of_classes)_.

### Smoothing

Smoothing is enabled by default, and it's useful to deal with unknown features by not returning a flat zero probability.

You can change the `*smoothing-factor*` var to change the smoothing intensity, although the default value is usually good enough.

Although it's not recommended, you can turn smoothing off by setting the `*smoothing-factor*` var to zero.

### Feature extraction

#### Brazilian Portuguese

We provide a simple implementation for Brazilian Portuguese based on the work done in [Apache Lucene](http://lucene.apache.org/core/), which is enabled by default.

#### Providing Your Own Feature Extractor

To provide your own feature extraction implementation:

First, implement the required functions in a namespace of your choice; use `clj-thatfinger.extractors.brazilian-simple-extractor` as reference. Then, change the `*extractor-module*` var to point to that namespace.

### Database integration

#### MongoDB

MongoDB integration is enabled by default. Change the `*mongodb-<setting>*` vars to point to your MongoDB instance.

#### Providing Your Own Database Layer

First, implement the required functions in a namespace of your choice; use `clj-thatfinger.db.mongodb` as reference. Then, change the `*db-module*` var to point to that namespace.

## TODO

* RESTful API

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.