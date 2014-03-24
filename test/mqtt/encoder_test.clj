(ns mqtt.encoder-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.encoder
        mqtt.packets.common)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest error-fn-called-when-error-test
  (testing "error-fn is called when an encoder exception is thrown"
    (let [calls (atom [])
          error-fn (fn [error] (swap! calls conj error))
          encoder (make-encoder :error-fn error-fn)
          out     (Unpooled/buffer 24)]
      (.encode encoder nil {:type "JUNK!"} out)
      (is (= 1 (count @calls)))))

  (testing "it swallows errors when no error-fn"
    (let [encoder (make-encoder)
          out     (Unpooled/buffer 24)]
      (try 
        (.encode encoder nil {:type "JUNK!"} out)
        (is :not-thrown)
        (catch Exception e
          (is (nil? e)))))))
