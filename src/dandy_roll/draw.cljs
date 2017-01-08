(ns dandy-roll.draw)

(defrecord DeferredDraw [canvas promise])

(defprotocol Drawable
  "Represents a resource being drawn on canvas"
  (draw [this canvas x y options] "Draws the resource to a canvas")
  (width [this canvas] "Returns the width of the drawable item")
  (height [this canvas] "Returns the height of the drawable item"))

;; Define a record representing an image being
;; applied as a watermark
(defrecord WatermarkImage [img]
  Drawable
  (draw [_ canvas x y {:keys [alpha] :or {alpha 1.0}}]
    (as-> (.getContext canvas "2d") ctx
          (do (set! (.-globalAlpha ctx) alpha) ctx)
          (.drawImage ctx img x y)))
  (width [_ _] (.-width img))
  (height [_ _] (.-height img)))

(defn- make-font [size family]
  (str size "px " family))

(defrecord WatermarkText [text font-size-px font-family fill]
  Drawable
  (draw [_ canvas x y {:keys [alpha] :or {alpha 1.0}}]
    (as-> (.getContext canvas "2d") ctx
          (do
            (set! (.-globalAlpha ctx) alpha)
            (set! (.-fillStyle ctx) fill)
            (set! (.-font ctx) (make-font font-size-px font-family))
            (set! (.-textBaseline ctx) "top")
            ctx)
          (.fillText ctx text x y)))
  (width [_ canvas]
    (as-> (.getContext canvas "2d") ctx
          (do
            (set! (.-font ctx) (make-font font-size-px font-family))
            (.measureText ctx text))
          (.-width ctx)))
  (height [_ _] font-size-px))

(defn make-image
  [img]
  (->WatermarkImage img))

(defn make-text
  [text font-size font-family fill]
  (->WatermarkText text font-size font-family fill))

(defn safe-draw
  "Calls the context save function before executing the draw function.
   Calls the context restore function after executing the draw function.
   Returns the canvas"
  [canvas draw-fn]
  (let [ctx (.getContext canvas "2d")]
    (.save ctx)
    (draw-fn canvas)
    (.restore ctx)
    canvas))

(defn defer
  "Creates a DeferredDraw. A DeferredDraw is a simple
   record that encapsulates a canvas being drawed on
   and a promise representing a future action to take
   on that canvas"
  [canvas promise]
  (->DeferredDraw canvas promise))

(defn draw-image
  "Draw an image onto the given canvas"
  [canvas img]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-width canvas) (.-width img))
    (set! (.-height canvas) (.-height img))
    (.drawImage ctx img 0 0)
    canvas))
