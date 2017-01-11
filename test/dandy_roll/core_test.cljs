(ns dandy-roll.core-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest defcard defcard-doc dom-node]]
            [dandy-roll.core :as dr]))

(defcard-doc
  "##`(watermark resource & handlers)`

   Loads the given resource and applies all handlers to it. `resource` may be any of the following
   types: `File`, `Image`, `String` (uri), or `HTMLImageElement`.

  `handlers` is a collection of
   handler functions. A handler function accepts and returns a `DeferredDraw` record. This record
   is effectively a map with a `:canvas` key and a `:promise` key. The promise should we waited up   on via the functions provided in `dandy-roll.promise`, and `:canvas` is an `HTMLCanvasElement`    or an object with a comparable API.

   `dandy-roll.core` ships with several useful handlers as well: `with-image`, `with-text`,
   `append`, and `to-blob`.")

(defcard watermarking-with-images
  "### Example: watermarking with images

  ```
  (watermark \"target.jpeg\"
    (with-image \"mark.jpeg\" lower-right)
    (append node))
  ```"
  (dom-node
    (fn [_ node]
      (set! (.-innerHTML node) "")
      
      (dr/watermark "target.jpeg"
        (dr/with-image "mark.jpeg" dr/lower-right)
        (dr/append node)))))

(defcard watermarking-with-text
  "### Example: watermarking with text

  ```
  (watermark \"target.jpeg\"
    (with-text \"I CAN HAZ\" 32 \"Impact\" \"#fff\" center-top)
    (with-text \"FUN CAT MEMES?\" 32 \"Impact\" \"#000\" center-bottom)
    (append node))
  ```"
  (dom-node
    (fn [_ node]
      (set! (.-innerHTML node) "")

      (dr/watermark "target.jpeg"
        (dr/with-text "I CAN HAZ" 32 "Impact" "#fff" dr/center-top)
        (dr/with-text "FUN CAT MEMES?" 32 "Impact" "#000" dr/center-bottom)
        (dr/append node)))))

(defcard watermarking-with-opacity
  "### Example: controlling watermark opacity

   The bundled handlers and any handler created using `dandy-roll.core/drawer`
   can be invoked to return a configured handler. The `Drawable` protocol only
   supports one option for now, and that is the `:alpha` option.

   ```
   (watermark \"target.jpeg\"
     (with-image \"mark.jpeg\" (upper-left { :alpha 0.6 }))
     (append node))
   ```"
  (dom-node
    (fn [_ node]
      (set! (.-innerHTML node) "")
      
      (dr/watermark "target.jpeg"
        (dr/with-image "mark.jpeg" (dr/upper-left { :alpha 0.6 }))
        (dr/append node)))))

(defcard-doc
  "## Core handlers

   `dandy-roll.core` ships with several handlers for common operations.

   ##`(with-image resource draw)`

   Invoking this function returns a handler that loads the given `resource`
   and draws it on the watermark target with the given `draw` function. `resource`
   can be type that the `watermark` function is capable of loading.
  
   `draw` is a function that is called with a first arugment of type `HTMLCanvasElement` or
    an object with a comparable API. The second argument is a type implementing the `Drawable`
    protocol from `dandy-roll.draw`.

   ## `(with-text text font-size family fill draw)`

   Invoking this function returns a handler that draws text to the watermark target.
   `font-size` is a numeric pixel value. `family` is a string value representing the
   font-family that will be used. `fill` represents the fill color of the text.
   `draw` follows the same semantics used by `with-image`.

   ## `(append element)`

   Invoking this function returns a handler that appends the watermarked result to
   the given element. `element` is appended to via `goog.dom/appendChild`

   ## `(to-blob on-blobbed)`

   Invoking this function returns a handler that converts the watermarked result to
   a `Blob`. A `Blob` is useful for uploading a watermarked image. `on-blobbed` is invoked
   with the `Blob` object once conversion is complete

   ### Example:

   ```
   (watermark \"/target.jpeg\"
     (with-image \"/mark.jpeg\" lower-right)
     (to-blob upload))
   ```")

(defcard-doc
  "## Core draw functions

   `dandy-roll.core` ships with some commonly used draw functions, as well
   as a useful helper for generating them. A draw function receives a `Drawable` record
   and an `HTMLCanvasElement`. If the core draw functions will not suffice, then it is
   pretty simple to create new ones.

   ## `(drawer x-fn y-fn)`
   
   All core draw functions are defined via the `drawer` function. `x-fn` and `y-fn`
   are functions that are called with the `HTMLCanvasElement` and the `Drawable` record
   in that order. `x-fn` should return the x coordinate to draw at, and `y-fn` should return
   the y coordinate draw at.

   `drawer` returns a multi-arity function. Invoke the draw function with one argument to
   return a function that performs the draw with a set of options:
   
   ```
   (upper-left { :alpha 0.5 })
   ```

   Invoke the draw function with two arguments (a `Drawable` and an `HTMLCanvasElement`),
   to immediately perform the draw operation with no configuration:

   ```
   (upper-left (make-image img) canvas)
   ```
   
   `drawer` is only useful for creating simple draw functions that only deal in x and y
    placement. For more complex draw operations, a custom function should used.

    ```
    ;; Move the watermark 10 pixels from the top and left edges
    (def upper-left
      (drawer #(identity 10) #(identity 10)))
    ```
    
    The following functions are included:

    * lower-left
    * lower-right
    * upper-left
    * upper-right
    * center
    * center-top
    * center-bottom")
