(ns dandy-roll.blob-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest]]
            [dandy-roll.blob :as b]))

(deftest blob-test
  (testing "blob creation"
    (testing "with a data url"
      (let [url (str "data:image/png;base64,", (js/btoa "hi"))
            blob (b/blob url)]
        (is (= "image/png" (.-type blob)))
        (is (= 2 (.-size blob)))))
    (testing "with a malformed data url still returns blob"
      (let [blob (b/blob "oh hai")]
        (is (re-find #"function Blob" (str (.-constructor blob))))))
    (testing "with incorrect input"
      (is (thrown-with-msg? js/Error #"Assert failed" (b/blob 2))))))
