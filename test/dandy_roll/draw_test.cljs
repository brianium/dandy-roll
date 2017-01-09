(ns dandy-roll.draw-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest defcard]]
            [sablono.core :as sab]
            [goog.dom :as gdom]
            [goog.dom.forms :as gforms]
            [dandy-roll.draw :as d]
            [dandy-roll.canvas :as c]
            [dandy-roll.promise :as p]
            [dandy-roll.load :as l]))

(deftest watermark-text-test
  (testing "dimensions of watermark text"
    (let [text (d/make-text "hello" 38 "Arial" "#fff")
          canvas (c/get-canvas)
          width (d/width text canvas)
          height (d/height text canvas)]
      (is (number? width))
      (is (= 38 height)))))

(defn alpha [element]
  "Get the alpha value from an element"
  (-> (gdom/getElement element)
      (gforms/getValue)
      (js/parseInt)
      (/ 100)))

(defn draw-text []
  "Handles drawing text via the protocol method. I would love
   for this to be more stateful, but canvas kind of complicates
   things here. This should be more than enough to illustrate
   the API"
  (let [canvas (gdom/getElement "text-canvas")
        ctx (.getContext canvas "2d")
        text-value (-> (gdom/getElement "text-input")
                       (gforms/getValue))
        alpha-percent (alpha "text-alpha")
        text (d/make-text text-value 20 "Arial" "#fff")]
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
    (d/draw text canvas 10 10 {:alpha alpha-percent})))

(defn text-code
  [{:keys [text alpha]}]
  (str
    "(def text (make-text \"" text "\" 20 \"Arial\" \"#fff\"))\n"
    "(draw text canvas 10 10 {:alpha " alpha "})"))

(defcard drawing-watermark-text
  "### Watermarking a canvas with text

  Text is drawn via the `Drawable` protocol method `draw`. Text
  is handled by the record `WatermarkText` implementing this protocol.

  Try typing something to see this protocol method at work."
  (fn [data _]
    (sab/html
      [:div
       [:pre (text-code @data)]
       [:canvas {:id "text-canvas"
                 :width "300"
                 :height "40"
                 :style {"backgroundColor" "black"}}]
       [:form
        [:input {:type "text"
                 :id "text-input"
                 :placeholder "Write something!"
                 :onChange (fn [e]
                             (draw-text)
                             (swap! data assoc :text (.. e -target -value)))
                 :style {"width" "295px"}}]
        [:div
         [:label "opacity: "]
         [:input {:type "range"
                  :id "text-alpha"
                  :min "0"
                  :max "100"
                  :defaultValue "100"
                  :onChange (fn [e]
                              (let [val (js/parseInt (.. e -target -value) 10)
                                    pct (/ val 100)]
                                (draw-text)
                                (swap! data assoc :alpha pct)))
                  :style {"width" "235px"
                          "position" "relative"
                          "top" "5px"}}]]]]))
  {:text "" :alpha 1.0})

(deftest watermark-image-test
  (let [pr (l/load-image "/mark.jpeg")
        canvas (c/get-canvas)]
    (async done
      (p/then pr (fn [img]
                 (let [watermark (d/make-image img)
                       width (d/width watermark canvas)
                       height (d/height watermark canvas)]
                   (is (= 90 width))
                   (is (= 90 height))
                   (done)))))))

(defn image-file []
  "Get the file object out of the image input"
  (-> (gdom/getElement "image-input")
      (aget "files")
      (array-seq)
      (first)))

(defn draw-image [file]
  "Given a file object representing a loaded image, draw it"
  (let [pr (l/load-image file)
        canvas (gdom/getElement "image-canvas")
        ctx (.getContext canvas "2d")
        alpha-percent (alpha "image-alpha")]
    (-> (p/then pr d/make-image)
        (p/then (fn [image]
                  (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
                  (d/draw image canvas 10 10 {:alpha alpha-percent}))))))

(defcard drawing-watermark-img
  "### Watermarking a canvas with an image

   Images are drawn via the `Drawable` protocl method `draw`. Images are handled
   by the record `WatermarkImage` implementing this protocol.

   Try uploading an image to see this protocol method at work."
  (sab/html [:div
             [:canvas {:id "image-canvas"
                       :width "300"
                       :height "300"
                       :style {"backgroundColor" "#000"}}]
             [:form
              [:label "Watermark image: "]
              [:input {:type "file"
                       :id "image-input"
                       :onChange (fn []
                                   (let [file (image-file)]
                                     (draw-image file)))}]
              [:input {:type "range"
                       :defaultValue "100"
                       :id "image-alpha"
                       :min "0"
                       :max "100"
                       :step "1"
                       :onChange (fn []
                                   (let [file (image-file)]
                                     (draw-image file)))
                       :style {"display" "block"
                               "width" "295px"}}]]]))
