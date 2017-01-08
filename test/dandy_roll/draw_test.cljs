(ns dandy-roll.draw-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard]]
            [sablono.core :as sab]
            [goog.dom :as gdom]
            [dandy-roll.draw :as d]
            [dandy-roll.canvas :as c]))

(deftest watermark-text-test
  (testing "dimensions of watermark text"
    (let [text (d/make-text "hello" 38 "Arial" "#fff")
          canvas (c/make-canvas)
          width (d/width text canvas)
          height (d/height text canvas)]
      (is (number? width))
      (is (= 38 height)))))

(defn draw-text []
  "Handles drawing text via the protocol method. I would love
   for this to be more stateful, but canvas kind of complicates
   things here. This should be more than enough to illustrate
   the API"
  (let [canvas (gdom/getElement "text-canvas")
        ctx (.getContext canvas "2d")
        text-input (gdom/getElement "text-input")
        text-value (.-value text-input)
        alpha-range (gdom/getElement "text-alpha")
        alpha-value (js/parseInt (.-value alpha-range) 10)
        alpha-percent (/ alpha-value 100)
        text (d/make-text text-value 20 "Arial" "#fff")]
    (do (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
        (d/draw text canvas 10 10 {:alpha alpha-percent}))))

(defn text-code
  [{:keys [text alpha]}]
  (str
    "(def text (make-text \"" text "\" 20 \"Arial\" \"#fff\"))\n"
    "(draw text canvas 10 10 {:alpha " alpha "})"))

(defcard drawing-watermark-text
  "### Watermarking a canvas with text

  Text is drawn via the Drawable protocol method `draw`. Text
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
                  :onChange (fn [e]
                              (let [val (js/parseInt (.. e -target -value) 10)
                                    pct (/ val 100)]
                                (draw-text)
                                (swap! data assoc :alpha pct)))
                  :value "100"
                  :style {"width" "235px"
                          "position" "relative"
                          "top" "5px"}}]]]]))
  {:text "" :alpha 1.0})
