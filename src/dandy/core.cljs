(ns dandy.core
  (:require [dandy.promise :refer [then]]
            [dandy.canvas :refer [get-canvas data-url]]
            [dandy.load :refer [load-image]]
            [dandy.draw :as draw]
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

(defn lower-right
  ([options]
   (fn [drawable canvas]
     (let [x (offset-width canvas drawable)
           y (offset-height canvas drawable)]
       (draw/safe-draw
         canvas
         #(draw/draw drawable canvas x y options)))))
  ([drawable canvas]
   (apply
     (lower-right {})
     [drawable canvas])))

(defn upper-right
  ([options]
   (fn [drawable canvas]
     (let [x (offset-width canvas drawable)]
       (draw/safe-draw
         canvas
         #(draw/draw drawable canvas x (draw/offset-y drawable) options)))))
  ([drawable canvas]
   (apply
     (upper-right {})
     [drawable canvas])))

(defn upper-left
  ([options]
   (fn [drawable canvas]
     (draw/safe-draw
       canvas
       #(draw/draw drawable canvas 10 (draw/offset-y drawable) options))))
  ([drawable canvas]
   (apply
     (upper-left {})
     [drawable canvas])))

(defn lower-left
  ([options]
   (fn [drawable canvas]
     (let [y (offset-height canvas drawable)]
       (draw/safe-draw
         canvas
         #(draw/draw drawable canvas 10 y options)))))
  ([drawable canvas]
   (apply
     (lower-left {})
     [drawable canvas])))

(defn center
  ([options]
   (fn [drawable canvas]
     (let [x (/ (- (.-width canvas) (draw/width drawable canvas)) 2)
           y (/ (- (.-height canvas) (draw/height drawable canvas)) 2)]
       (draw/safe-draw
         canvas
         #(draw/draw drawable canvas x y options)))))
  ([drawable canvas]
   (apply
     (center {})
     [drawable canvas])))

;;;; Bundled Handler Functions 

(defn with-image
  [resource draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(load-image resource))
        (then (fn [img] (draw (draw/make-image img) canvas)))
        (as-> promise (draw/defer canvas promise)))))

(defn with-text
  [text font-size font-family fill draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(draw (draw/make-text text font-size font-family fill) canvas))
        (as-> promise (draw/defer canvas promise)))))

(defn append
  "A handler that creates converts the canvas to an image
   and appends it to a given element"
  [element]
  (fn [{:keys [promise canvas]}]
    (-> (then promise data-url)
        (then make-image)
        (then (fn [img] (gdom/appendChild element img))))))

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
  (with-text "Oh Hai" 28 "Helvetica" "#fff" lower-left)
  (with-text "Oh Hai" 28 "Helvetica" "#fff" upper-right)
  (with-text "Oh Hai" 28 "Helvetica" "#fff" upper-left)
  (with-text "Oh Hai" 28 "Helvetica" "#fff" center)
  (append (.-body js/document)))
