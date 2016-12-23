(ns dandy-roll.load-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest defcard defcard-doc]]
            [sablono.core :as sab]
            [goog.dom :as gdom]
            [dandy-roll.load :as l]
            [dandy-roll.promise :refer [then]]))


(deftest load-image-from-image
  (let [img (js/Image.)
        p (l/load-image img)]
    (async done
      (then p (fn [i]
                (is (= i img))
                (done))))))

(deftest load-image-from-url
  (let [p (l/load-image "/mark.jpeg")]
    (async done
      (then p (fn [img]
                (is (re-find #"mark.jpeg" (.-src img)))
                (done))))))

(defcard image
  "Rendering an image to test loading an `HTMLImageElement`

  ![testing](/mark.jpeg \"test\")")

(deftest load-an-html-image-element
  (let [p (l/load-image (.querySelector js/document "img[title=test]"))]
    (async done
      (then p (fn [img]
                (is (re-find #"mark.jpeg" (.-src img)))
                (done))))))

(defcard-doc
  "### Loading an image from a File object
  
   Really the only safe way to watermark an image without
   leaking the original content

   Creating file objects without a file input is surprisingly difficult
   so we will exercise this load function via the following card.")

(defn append [event]
  "When the file input changes, load the image from File and append it.
   This function actually exercises the load-image function for type File"
  (let [input (.-target event)
        files (.-files input)
        parent (gdom/getParentElement input)
        container (gdom/getNextElementSibling parent)
        file (first (array-seq files))
        p (l/load-image file)]
    (then p (fn [img]
              (gdom/appendChild container img)
              (set! (.-value input) "")))))

(defn reset [event]
  "Clears all uploaded images from  the card"
  (let [parent (.. event -target -parentNode)
        container (gdom/getNextElementSibling parent)]
    (gdom/removeChildren container)))

(defcard load-image-from-file
  "We should be able to upload an image and create an element from it."
  (sab/html [:div
             [:div {:style {"margin-bottom" "20px"}}
              [:input {:type "file" :onChange append}]
              [:button {:type "button" :onClick reset} "reset images"]]
             [:div]]))
