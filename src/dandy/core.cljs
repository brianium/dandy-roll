(ns dandy.core
  (:require [dandy.promise :refer [then]]
            [dandy.canvas :refer [get-canvas data-url]]
            [dandy.load :refer [load-image]]
            [goog.dom :as gdom]))

(defrecord DeferredDraw [canvas promise])

(defn defer-draw [canvas promise]
  (->DeferredDraw canvas promise))

(defn with-image
  [resource draw]
  (fn [{:keys [promise canvas]}]
    (-> (then promise #(load-image resource))
        (then (fn [img] (draw img canvas)))
        (as-> promise (defer-draw canvas promise)))))

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

(defn draw-image [canvas img]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-width canvas) (.-width img))
    (set! (.-height canvas) (.-height img))
    (.drawImage ctx img 0 0)
    canvas))

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
