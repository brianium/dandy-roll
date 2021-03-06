(ns dandy-roll.load
  (:require [dandy-roll.promise :refer [promise then resolve]]
            [goog.events :refer [listen]]))

;; This may need to be refined - should cover MOST bases
(defonce url-pattern #"(?:https?://)?([^/]+)")

(defn- resolve-reader
  "Resolves a promise with an image created from a FileReader"
  [reader image promise]
  (do
    (listen image "load" #(resolve promise image))
    (set! (.-src image) (.-result reader))))

(defn- set-src!
  "Sets the src attribute of the image. If it is determined
   that the source domain and the current domain are different,
   then the crossOrigin property of the image will be set to 'Anonymous'"
  [img src-url]
  (let [host (.. js/window -location -hostname)
        url-host (second (re-find url-pattern src-url))]
    (if (not= host url-host)
      (set! (.-crossOrigin img) "Anonymous"))
    (set! (.-src img) src-url)))

;; Dispatch loading strategy by type. All load methods should return a promise
(defmulti load-image type)

;; Resolve an image element immediately
(defmethod load-image js/Image [img]
  (-> (promise)
      (resolve img)))

;; HTMLImageElement has the same behavior as an Image
(defmethod load-image js/HTMLImageElement [img]
  (-> (promise)
      (resolve img)))

;; A string argument means we are loading a URL
(defmethod load-image js/String [url]
  (let [p (promise)
        img (js/Image.)]
    (listen img "load" #(resolve p img))
    (set-src! img url)
    p))

;; A File argument leverages a FileReader to create an image
(defmethod load-image js/File [file]
  (let [p (promise)
        reader (js/FileReader.)
        img (js/Image.)]
    (listen reader "loadend" #(resolve-reader reader img p))
    (.readAsDataURL reader file)
    p))
