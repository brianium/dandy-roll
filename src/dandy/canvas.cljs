(ns dandy.canvas)

(defonce canvas-pool (atom #queue []))

(defn make-canvas []
  (.createElement js/document "canvas"))

(defn get-canvas
  "Fetches canvas from a 'pool' of canvas elements."
  []
  (let [pool (if (peek @canvas-pool)
               @canvas-pool
               (swap! canvas-pool conj (make-canvas)))
        canvas (peek pool)]
    (swap! canvas-pool pop)
    canvas))

(defn data-url
  "Return the data url of a canvas.

   Fetching a data url
   causes the canvas to be released back to the pool."
  [canvas]
  (let [url (.toDataURL canvas)
        context (.getContext canvas "2d")]
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (swap! canvas-pool conj canvas)
    url))
