(ns dandy.load
  (:require [dandy.promise :refer [promise then resolve]]
            [goog.events :refer [listen]]))

(defonce type-pattern #"function ([\w]+)")

(defn- type-str
  "Returns the type of x as a string"
  [x]
  (->> (type x)
       (str)
       (re-find type-pattern)
       (second)))

(defn- resolve-reader
  "Resolves a promise with an image created from a FileReader"
  [reader image promise]
  (do
    (listen image "load" #(resolve promise image))
    (set! (.-src image) (.-result reader))))

;; Dispatch loading strategy by type. All load methods should return a promise
(defmulti load-image type-str)

;; Resolve an image element immediately
(defmethod load-image "HTMLImageElement" [img]
  (-> (promise)
      (resolve img)))

;; A string argument means we are loading a URL
(defmethod load-image "String" [url]
  (let [p (promise)
        img (js/Image.)]
    (listen img "load" #(resolve p img))
    (set! (.-src img) url)
    p))

;; A File argument leverages a FileReader to create an image
(defmethod load-image "File" [file]
  (let [p (promise)
        reader (js/FileReader.)
        img (js/Image.)]
    (listen reader "loadend" #(resolve-reader reader img p))
    (.readAsDataURL reader file)
    p))
