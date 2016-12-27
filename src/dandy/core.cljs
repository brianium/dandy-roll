(ns dandy.core
  (:require [dandy.promise :refer [then]]
            [dandy.canvas :refer [get-canvas data-url]]
            [dandy.load :refer [load-image]]))

(defn with-image
  [resource draw]
  (fn [promise canvas]
    (-> (then promise #(load-image resource))
        (then (fn [img] (draw img canvas))))))

(defn lower-right [img canvas]
  (let [ctx (.getContext canvas "2d")
        x (- (.-width canvas) (+ 10 (.-width img)))
        y (- (.-height canvas) (+ 10 (.-height img)))]
    (.save ctx)
    (.drawImage ctx img x y)
    (.restore ctx)
    canvas))

(defn draw-image [img canvas]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-width canvas) (.-width img))
    (set! (.-height canvas) (.-height img))
    (.drawImage ctx img 0 0)
    canvas))

(defn watermark
  [resource handler]
  (let [canvas (get-canvas)
        p (load-image resource)]
    (-> (then p #(draw-image %1 canvas))
        (handler canvas)
        (then data-url)
        (then (.-log js/console)))))

(watermark "http://placehold.it/310x310"
  (with-image "http://placehold.it/155x155" lower-right))
