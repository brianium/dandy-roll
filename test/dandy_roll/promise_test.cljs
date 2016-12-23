(ns dandy-roll.promise-test
  (:require [cljs.test :refer-macros [is async]]
            [devcards.core :refer-macros [deftest]]
            [dandy-roll.promise :refer [then resolve promise reject]]))

(deftest promise-api-resolve
  (async done
    (-> (promise)
        (then inc)
        (then inc)
        (then dec)
        (resolve 0)
        (then (fn [int]
                (is (= 1 int))
                (done))))))

(deftest promise-api-reject
  (async done
    (-> (promise)
        (then inc (fn [reason]
                    (is (= "error message" reason))
                    (done)))
        (reject "error message"))))
