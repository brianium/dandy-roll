(ns dandy.draw)

(defrecord DeferredDraw [canvas promise])

(defn defer-draw
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
