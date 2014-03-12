(ns mqtt.packets.pingreq-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.pingreq)
  (:import [io.netty.buffer Unpooled]))

(deftest pingreq-validate-message-test
  (testing "returns when valid"
    (let [packet {:type :pingreq}]
      (is (= packet (validate-message packet))))))

(deftest encoding-pingreq-packet-test
  (testing "when encoding a simple pingreq packet"
    (let [encoder (make-encoder)
          packet  {:type :pingreq}
          out     (Unpooled/buffer 2)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xC0
                        ;; remaining length
                        0x00)))))))

(deftest decoding-pingreq-packet-test
  (testing "when parsing a simple Pingreq packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0xC0 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :pingreq (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded)))))))
