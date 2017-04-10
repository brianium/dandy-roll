# Dandy Roll

### Current release:

[![Clojars Project](https://img.shields.io/clojars/v/brianium/dandy-roll.svg)](https://clojars.org/brianium/dandy-roll)

An improved port of [watermark.js](https://github.com/brianium/watermarkjs), Dandy Roll is a ClojureScript library for watermarking images. It is a small layer on top of the HTML canvas API.

Most people immediately point out how useless watermarking in the browser is:

> You can still get the original source of the image by looking at source!

Dandy Roll supports watermarking images from all sorts of sources. It can watermark the following types:

* `Image`
* `HTMLImageElement`
* `String` - that is a url
* `File` - that is an uploaded image!

The only sure fire way to safely watermark an image without exposing the original source is to do so via the `File` type.

## Example

Most of the relevant functions needed are inside `dandy-roll.core`

### Watermarking with an image

```clojure
;; load a url and watermark it
(watermark "/mycat.jpeg"
  (with-image "/logo.png" lower-right)
  (append (.-body js/document)))

;; load an image from a file input, watermark it, and upload it
(def files (array-seq (.-files my-input)))

(defn upload [blob] ,,,)

(watermark (first files)
  (with-image "/logo.png" upper-left)
  (to-blob upload))

;; watermark an image with lots of credit due
(watermark "/mycat.jpeg"
  (with-image "/logo1.png" lower-right)
  (with-image "/logo2.png" lower-left)
  (with-image "/logo3.png" upper-left)
  (with-image "/logo4.png" upper-right)
  (with-image "/logo5.png" center)
  (append (.-body js/document)))

;; add alpha transparency
(watermark "/mycat.jpeg"
  (with-image "/logo.png" (lower-right { :alpha 0.4 }))
  (to-blob upload))
```

### Watermarking with text

Sometimes it's just fun and cool to write text on an image.

```clojure
(watermark "lolcat.jpeg"
  (with-text "I CAN HAZ" 32 "Impact" "#fff" center-top)
  (with-text "CAT MEMEZ?" 32 "Impact" "#fff" center-bottom)
  (append some-element))
```

Keep in mind, any font families used with text need to be loaded or they
will not be applied correctly.

## Tests

I started out using PhantomJS for automated tests, but it seems to be lacking
in the `Blob` department and ES6 department. Seeing how this library is small
and very visual, I opted for the amazing [Devcards](https://github.com/bhauman/devcards)
library by Bruce Hauman. It offers a hot reloaded test environment that doubles down as documentation
and tests via `cljs.test`

To run the tests during development just run `lein figwheel devcards-test` and hit `localhost:3449/tests.html`

To run them right now head to the [standalone devcard deployment](https://brianium.github.io/dandy-roll/) on GitHub pages. Run tests
for a namespace by clicking on that namespace's link.

## Documentation

Devcards is my documentation tool as well. The [core test](https://brianium.github.io/dandy-roll/#!/dandy_roll.core_test) not only
tests the core API, but provides documentation for it as well.

## Todo

* Support Node.js targets
