(ns dandy-roll.promise)

(defprotocol IDeferrable
  "A protocol to simplify working with native JS promises"
  (resolve [this value] "resolve a promise")
  (reject [this reason] "reject a promise")
  (then [this resolved] [this resolved rejected] "handle the result of a promise"))

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
     (reject [_ reason]
       (.reject deferred reason)
       (make-deferrable deferred promise))
     (then
       ([_ resolved]
        (make-deferrable deferred (.then promise resolved)))
       ([_ resolved rejected]
        (make-deferrable deferred (.then promise resolved rejected)))))))

(defn promise
  "Creates a native JavaScript promise supporting the protocol IDeferrable"
  []
  (make-deferrable))
