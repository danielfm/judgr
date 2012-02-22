# clj-thatfinger

clj-thatfinger is a Multi-class Naïve Bayes Classifier written in Clojure.

This project is still in early stage of development, so use it at your own risk.

## Requirements

* [Clojure 1.3](http://clojure.org)
* [Leiningen 1.6](http://github.com/technomancy/leiningen)

## Features

* Supports multiclass classification
* Biased and unbiased class probabability
* Configurable [Laplace Smoothing](http://en.wikipedia.org/wiki/Additive_smoothing)
* Configurable threshold validation
* K-fold cross-validation
* [MongoDB](http://mongodb.org)-ready
* RESTful API (soon)

## Usage

TODO: write.

## Extending The Application

The application settings can be found at `src/clj_thatfinger/settings.clj`.

### Supported Classes

Change the `*classes*` var to the classes you want to use when flagging messages, along with their thresholds.

#### Threshold Validation

If threshold validation is enabled, i.e. `*threshold-enabled*` is true, a message will only be flagged as a class if it's probability is at least _X_ times greater than the second highest probability.

For example, if the probabilities for a message are `{:ok 0.45 :offensive 0.55}`, and their thesholds are 2 and 1, respectively, the message will be flagged with the value defined in `*class-unknown*` var, which is `:unknown` by default.

#### Using Unbiased Class Probabilities

By default, class probabilities are calculated in a _biased_ fashion, that is, considering the number of messages flagged in each class. For example, if there's no message flagged as `:offensive`, the probability _P(offensive) = 0_. Similarly, if there's 3 offensive messages out of 10, then _P(offensive) = 3/10_.

If the `*classes-unbiased*` var is set to true, the probability _P(any_class) = 1/(number of classes)_.

### Smoothing

Smoothing is enabled by default, and it's useful to deal with unknown features by not returning a flat zero probability.

You can change the `*smoothing-factor*` var to change the smoothing intensity, although the default value of 1 is usually good enough.

Although it's not recommended, you can turn smoothing off by setting the `*smoothing-factor*` var to zero.

### Feature extraction

#### Brazilian Portuguese

We provide a simple implementation for Brazilian Portuguese, a porter stemmer actually, based on the work done in [Apache Lucene](http://lucene.apache.org/core/), which is enabled by default.

The Porter stemming is a process for removing the commoner morphological and inflexional endings from words. Its main use is as part of a term normalisation process.

#### Providing Your Own Feature Extractor

To provide your own feature extraction implementation:

First, implement the required functions in a namespace of your choice. Use `clj-thatfinger.extractors.brazilian-simple-extractor` as reference. Then, change the `*extractor-module*` var to point to your namespace.

### Database integration

#### MongoDB

MongoDB integration is enabled by default. Change the `*mongodb-<setting>*` vars to point to your MongoDB instance.

#### Providing Your Own Database Layer

First, implement the required functions in a namespace of your choicei. Use `clj-thatfinger.db.mongodb` as reference. Then, change the `*db-module*` var to point to your namespace.

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for more information.