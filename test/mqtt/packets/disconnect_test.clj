(ns mqtt.packets.disconnect-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.disconnect)
  (:import [io.netty.buffer Unpooled]))

(deftest disconnect-validate-message-test
  (testing "returns nil when valid"
    (let [packet {:type :disconnect}]
      (is (= packet (validate-message packet))))))

(deftest encoding-disconnect-packet-test
  (testing "when encoding a simple disconnect packet"
    (let [encoder (make-encoder)
          packet  {:type :disconnect}
          out     (Unpooled/buffer 2)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xE0
                        ;; remaining length
                        0x00)))))))

(deftest decoding-disconnect-packet-test
  (testing "when parsing a simple disconnect packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0xE0 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :disconnect (:type decoded)))))))
