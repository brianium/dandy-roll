(ns dandy.promise)

(defprotocol IDeferrable
  "A protocol to simplify working with native JS promises"
  (resolve [this value] "resolve a promise")
  (reject [this] "reject a promise")
  (then [this fn] "handle the result of a promise"))

(defn- make-deferrable
  "A factory function for creating deferrables"
  ([]
   (let [deferred #js {}
         promise (js/Promise. (fn [res rej]
                                (set! (.-resolve deferred) res)
                                (set! (.-reject deferred) rej)))]
     (make-deferrable deferred promise)))
  ([deferred, promise]
   (specify! promise
     IDeferrable
     (resolve [_ value]
       (.resolve deferred value)
       (make-deferrable deferred promise))
     (reject [_]
       (.reject deferred)
       (make-deferrable deferred promise))
     (then [_ fn]
       (make-deferrable deferred (.then promise fn))))))

(defn promise
  "Creates a native JavaScript promise supporting the protocol IDeferrable"
  []
  (make-deferrable))
