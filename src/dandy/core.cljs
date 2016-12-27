(ns dandy.core
  (:require [dandy.promise :refer [then]]
            [dandy.canvas :refer [get-canvas data-url]]
            [dandy.load :refer [load-image]]
            [goog.dom :as gdom]))

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

(defn upper-left [img canvas]
  (let [ctx (.getContext canvas "2d")
        x 10
        y 10]
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
  [resource handler1 handler2 commit]
  (let [canvas (get-canvas)
        p (load-image resource)]
    (-> (then p #(draw-image %1 canvas))
        (handler1 canvas)
        (handler2 canvas)
        (then data-url)
        (commit))))

(defn append [promise]
  (then promise (fn [data-url]
                  (let [img (js/Image.)
                        body (.-body js/document)]
                    (set! (.-src img) data-url)
                    (gdom/appendChild body img)))))

(watermark "http://placehold.it/310x310"
  (with-image "http://placehold.it/155x155" lower-right)
  (with-image "http://placehold.it/50x50" upper-left)
  append)
