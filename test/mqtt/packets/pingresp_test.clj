(ns mqtt.packets.pingresp-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.pingresp)
  (:require [mqtt.decoding-utils :as du])
  (:import [io.netty.buffer Unpooled]))

(deftest pingresp-validate-message-test
  (testing "returns when valid"
    (let [packet {:type :pingresp}]
      (is (= packet (validate-message packet))))))

(deftest encoding-pingresp-packet-test
  (testing "when encoding a simple pingresp packet"
    (let [encoder (make-encoder)
          packet  {:type :pingresp}
          out     (Unpooled/buffer 2)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             [;; fixed header
              (du/unsigned-byte 0xD0)
              ;; remaining length
              0x00])))))

(deftest decoding-pingresp-packet-test
  (testing "when parsing a simple pingresp packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0xD0 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :pingresp (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded)))))))
