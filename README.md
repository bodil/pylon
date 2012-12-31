# <img align="right" src="https://raw.github.com/bodil/pylon/master/pylon.gif"> Pylon

A Javascript class system in 100% Clojurescript.

## Installation

To use Pylon in your project, put the following in the `:dependencies`
vector of your `project.clj` file:

```clojure
[org.bodil/pylon "0.1.0"]
```

## Defining Classes

Use the `defclass` macro to build Javascript style classes using Pylon.

```clojure
(ns pylon.test
  (:require [pylon.classes])
  (:use-macros [pylon.macros :only [defclass]]))

(defclass Hello
  (defn constructor [name]
    (aset this "name" name))
  (defn hello []
    (console/log (str "Hello " (.-name this) "!"))))

(.hello (Hello. "Kitty"))
;; => "Hello Kitty!"
```

Note that all methods have a `this` symbol available to them, just
like in Javascript. Unlike in Javascript, it will always be bound to
the actual object instance, even when passing an instance method as a
callback.

## Inheritance

Pylon allows you to define inheritance using the `:extends` keyword,
and call superclass methods using the `super` macro.

```clojure
(ns pylon.test
  (:require [pylon.classes])
  (:use-macros [pylon.macros :only [defclass super]]))

(defclass Hello
  (defn constructor [name]
    (aset this "name" name))
  (defn hello []
    (console/log (str "Hello " (.-name this) "!"))))

(defclass HelloSailor :extends Hello
  (defn constructor []
    (super "sailor")))

(.hello (HelloSailor.))
;; => "Hello sailor!"
```

## Mixins

If you need multiple inheritance, you can use the `:mixin` keyword to
extend your prototype further. Note that we've left the world of
prototypal inheritance behind when we do this: properties are copied
from the mixin objects into your object prototype; it does not
actually add more parent prototypes, which would be impossible.

## Caveats

Don't use advanced optimisation when compiling projects that use
Pylon. It will break horribly. Simple optimisation works fine, so
stick with that.

# License

Copyright 2012 Bodil Stokke

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
