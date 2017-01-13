(ns dandy-roll.draw)

(defrecord DeferredDraw [canvas promise])

(defprotocol Drawable
  "Represents a resource being drawn on canvas"
  (draw [this canvas x y options] "Draws the resource to a canvas")
  (width [this canvas] "Returns the width of the drawable item")
  (height [this canvas] "Returns the height of the drawable item"))

(defn apply-context
  "Applies a map of context properties to a canvas. Properties should
   be valid context properties. Returns the canvas context"
  [canvas properties]
  (let [ctx (.getContext canvas "2d")]    
    (doseq [[k v] properties]
      (aset ctx (name k) v))
    ctx))

(defonce image-defaults {:globalAlpha 1.0})

;; Define a record representing an image being
;; applied as a watermark
(defrecord WatermarkImage [img]
  Drawable
  (draw [_ canvas x y config]
    (let [properties (merge image-defaults config)
          ctx (apply-context canvas properties)]
      (.drawImage ctx img x y)))
  (width [_ _] (.-width img))  
  (height [_ _] (.-height img)))

(defn- make-font [size family]
  (str size "px " family))

(defonce text-defaults {:globalAlpha 1.0
                        :textBaseline "top"})

;; Define a record representing some text bpeing
;; applied as a watermark
(defrecord WatermarkText [text font-size-px font-family fill]
  Drawable
  (draw [_ canvas x y config]
    (let [font (make-font font-size-px font-family)
          properties (merge text-defaults config, {:fillStyle fill :font font})
          ctx (apply-context canvas properties)]
      (.fillText ctx text x y)
      (when (:strokeStyle properties)
        (.strokeText ctx text x y))))
  (width [_ canvas]
    (as-> (.getContext canvas "2d") ctx
          (do
            (set! (.-font ctx) (make-font font-size-px font-family))
            (.measureText ctx text))
          (.-width ctx)))
  (height [_ _] font-size-px))

(defn make-image
  "Creates a WatermarkImage record"
  [img]
  (->WatermarkImage img))

(defn make-text
  "Creates a WatermarkText record"
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
