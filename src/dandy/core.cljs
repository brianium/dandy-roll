(ns dandy.core
  (:require [dandy.promise :refer [then]]
            [dandy.canvas :refer [get-canvas data-url]]
            [dandy.load :refer [load-image]]
            [dandy.draw :refer [defer-draw draw-image]]
            [goog.dom :as gdom]))

(defprotocol Drawable
  "Represents a resource being drawn on canvas"
  (draw [this canvas x y] "Draws the resource to a canvas")
  (width [this] "Returns the width of the drawable item")
  (height [this] "Returns the height of the drawable item"))

;; Define a record representing an image being
;; applied as a watermark
(defrecord WatermarkImage [img]
  Drawable
  (draw [_ canvas x y]
    (-> (.getContext canvas "2d")
        (.drawImage img x y)))
  (width [_] (.-width img))
  (height [_] (.-height img)))

(defn safe-draw [canvas draw-fn]
  (let [ctx (.getContext canvas "2d")]
    (.save ctx)
    (draw-fn canvas)
    (.restore ctx)
    canvas))

(defn with-image
  [resource draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(load-image resource))
      (then (fn [img] (draw (->WatermarkImage img) canvas)))
        (as-> promise (defer-draw canvas promise)))))

(defn lower-right [drawable canvas]
  (let [ctx (.getContext canvas "2d")
        x (- (.-width canvas) (+ 10 (width drawable)))
        y (- (.-height canvas) (+ 10 (height drawable)))]
    (safe-draw canvas #(draw drawable canvas x y))))

(defn upper-left [drawable canvas]
  (let [ctx (.getContext canvas "2d")]
    (safe-draw canvas #(draw drawable canvas 10 10))))

(defn watermark
  [resource & fns]
  (let [canvas (get-canvas)
        defer (partial defer-draw canvas)
        handler (apply comp (reverse fns))]
    (-> (load-image resource)
        (then (partial draw-image canvas))
        (defer)
        (handler))))

(defn make-image [url]
  (let [img (js/Image.)]
    (set! (.-src img) url)
    img))

(defn append [{:keys [promise canvas]}]
  (-> (then promise data-url)
    (then make-image)
    (then (fn [img]
            (let [body (.-body js/document)]
              (gdom/appendChild body img))))))

(watermark "http://placehold.it/310x310"
  (with-image "http://placehold.it/155x155" lower-right)
  (with-image "http://placehold.it/50x50" upper-left)
  append)
