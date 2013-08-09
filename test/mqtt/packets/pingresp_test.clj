(ns mqtt.packets.pingresp-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.pingresp)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest pingresp-encoding-test
  (testing "when encoding a simple Pingresp packet"
    (let [encoder (make-encoder)
          packet  {:type :pingresp}
          out     (Unpooled/buffer 2)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              (unsigned-byte 0xD0)
              ;; remaining length
              0x00])))))
