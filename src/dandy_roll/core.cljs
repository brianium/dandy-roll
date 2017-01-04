(ns dandy-roll.core
  (:require [dandy-roll.promise :refer [then]]
            [dandy-roll.canvas :refer [get-canvas data-url]]
            [dandy-roll.load :refer [load-image]]
            [dandy-roll.draw :as draw]
            [dandy-roll.blob :refer [blob]]
            [goog.dom :as gdom]))

(defn- make-image
  "Creates an image element with the given src"
  [src]
  (let [img (js/Image.)]
    (set! (.-src img) src)
    img))

(defn- offset-width
  [canvas drawable]
  (-
    (.-width canvas)
    (+ 10 (draw/width drawable canvas))))

(defn- offset-height
  [canvas drawable]
  (-
    (.-height canvas)
    (+
      (draw/offset-y drawable)
      (draw/height drawable canvas))))

;;;; Bundled Draw Functions

;;; Draw functions should be isolated units of
;;; work. They are called with a Drawable record
;;; and a canvas - It is the draw function that
;;; determines how the watermark is placed.
;;; The following functions should serve as examples
;;; and should cover many common use cases

(defn drawer
  "A function that returns a generic draw function.
   
   Accepts two arguments: a function for resolving x placement
   and a function for resolving y placement. Both functions are
   passed an HTMLCanvasElement and a Drawable record.

   The returned function can be passed directly to a handler or
   can be invoked with a map to return a configured draw function"
  [x-fn y-fn]
  (fn draw-fn
    ([options]
     (fn [drawable canvas]
       (let [x (x-fn canvas drawable)
             y (y-fn canvas drawable)]
         (draw/safe-draw
           canvas
           #(draw/draw drawable canvas x y options)))))
    ([drawable canvas]
     (apply
       (draw-fn {})
       [drawable canvas]))))

(def lower-right
  (drawer offset-width offset-height))

(def upper-right
  (drawer offset-width #(draw/offset-y %2)))

(def upper-left
  (drawer #(identity 10) #(draw/offset-y %2)))

(def lower-left
  (drawer #(identity 10) offset-height))

(def center
  (drawer
    #(/ (- (.-width %1) (draw/width %2 %1)) 2)
    #(/ (- (.-height %1) (draw/height %2 %1)) 2)))

;;;; Bundled Handler Functions 

(defn with-image
  "A handler that loads an additional image for drawing"
  [resource draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(load-image resource))
        (then (fn [img] (draw (draw/make-image img) canvas)))
        (as-> promise (draw/defer canvas promise)))))

(defn with-text
  "A handler that supports drawing text"
  [text font-size font-family fill draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(draw (draw/make-text text font-size font-family fill) canvas))
        (as-> promise (draw/defer canvas promise)))))

(defn append
  "A handler that converts the watermarked canvas to an image
   and appends it to a given element"
  [element]
  (fn [{:keys [promise canvas]}]
    (-> (then promise data-url)
        (then make-image)
        (then (fn [img] (gdom/appendChild element img))))))

(defn to-blob
  "A handler that converts the watermarked canvas to a blob object. The given
   function will be called with a blob object ater conversion is complete"
  [on-blobbed]
  (fn [{:keys [promise canvas]}]
    (-> (then promise data-url)
        (then blob)
        (then on-blobbed))))

;;;; Core Watermark Function

(defn watermark
  "Takes an image resource and a series of handler functions.

  An image resource can be a string url, an existing image element, or a File object.

  A handler is a function that receives a DeferredDraw record. DeferredDraw is essentially
  a map containing a :promise key and a :canvas key. A handler should always assume the promise
  must be waited upon via the protocol method 'then'."
  [resource & fns]
  (let [canvas (get-canvas)
        defer (partial draw/defer canvas)
        handler (apply comp (reverse fns))]
    (-> (load-image resource)
        (then (partial draw/draw-image canvas))
        (defer)
        (handler))))

(watermark "/target.jpeg"
  (with-text "Oh Hai" 28 "Helvetica" "#fff" lower-right)
  (with-image "/mark.jpeg" (lower-left { :alpha 0.5 }))
  (with-text "Oh Hai" 28 "Helvetica" "#fff" upper-right)
  (with-text "Oh Hai" 28 "Helvetica" "#fff" upper-left)
  (with-text "Oh Hai" 28 "Helvetica" "#fff" (center { :alpha 0.5 }))
  (to-blob (.-log js/console)))
