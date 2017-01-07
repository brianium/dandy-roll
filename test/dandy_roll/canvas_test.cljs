(ns dandy-roll.canvas-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest]]
            [dandy-roll.canvas :as c]))

(defn is-canvas [actual]
  (is (= (type actual) js/HTMLCanvasElement)))

(deftest make-canvas
  (testing "canvas creation"
    (let [canvas (c/make-canvas)]
      (is-canvas canvas))))

(deftest get-canvas
  (testing "it returns a canvas element"
    (let [canvas (c/get-canvas)]
      (is-canvas canvas)))
  (testing "it pulls from a pool queue"
    (let [canvas (c/get-canvas)
          url (c/data-url canvas)
          pooled (c/get-canvas)]
      (is (= canvas pooled)))))

(deftest data-url
  (testing "it returns a data url"
    (let [canvas (c/get-canvas)
          data-url (c/data-url canvas)]
      (is (string? data-url))))
  (testing "it places the canvas back into the pool"
    (let [canvas (c/get-canvas)
          total (count @c/canvas-pool)
          url (c/data-url canvas)]
      (is (= (count @c/canvas-pool) (inc total)))))
  (testing "it rejects non canvas elements"
    (is (thrown-with-msg?
          js/Error
          #"Assert failed"
          (c/data-url "lol not a canvas")))))
