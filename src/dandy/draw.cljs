(ns dandy.draw)

(defrecord DeferredDraw [canvas promise])

(defprotocol Drawable
  "Represents a resource being drawn on canvas"
  (draw [this canvas x y options] "Draws the resource to a canvas")
  (width [this] "Returns the width of the drawable item")
  (height [this] "Returns the height of the drawable item"))

;; Define a record representing an image being
;; applied as a watermark
(defrecord WatermarkImage [img]
  Drawable
  (draw
    [_ canvas x y {:keys [alpha] :or {alpha 1.0}}]
    (as-> (.getContext canvas "2d") ctx
          (do (set! (.-globalAlpha ctx) alpha) ctx)
          (.drawImage ctx img x y)))
  (width [_] (.-width img))
  (height [_] (.-height img)))

(defn make-watermark-image
  [img]
  (->WatermarkImage img))

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
