(ns mqtt.packets.suback-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.suback)
  (:require [mqtt.decoding-utils :as du])
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest suback-validate-message-test
  (testing "nil when valid"
    (is (= nil (validate-message {:type :suback
                                  :message-id 1
                                  :granted-qos [0]}))))

  (testing "it throws if no message-id"
    (is (thrown? EncoderException (validate-message {:type :suback
                                                     :granted-qos [0]}))))

  (testing "it throws if message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :suback
                                                     :message-id 0
                                                     :granted-qos [0]})))))

(deftest suback-encoding-test
  (testing "when encoding a simple suback packet"
    (let [encoder (make-encoder)
          packet  {:type :suback :message-id 5 :granted-qos [0]}
          out     (Unpooled/buffer 5)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             [;; fixed header
              (du/unsigned-byte 0x90)
              ;; remaining length
              0x03
              ;; message id
              0x00 0x05
              ;; granted qos(es)
              0x00]))))

  (testing "when encoding a suback for two topics"
    (let [encoder (make-encoder)
          packet  {:type :suback :message-id 6 :granted-qos [0 1]}
          out     (Unpooled/buffer 5)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             [;; fixed header
              (du/unsigned-byte 0x90)
              ;; remaining length
              0x04
              ;; message id
              0x00 0x06
              ;; granted qos(es)
              0x00
              0x01])))))

(deftest decoding-suback-packet-test
  (testing "when parsing a simple suback packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x90
                    ;; remaining length
                    0x03
                    ;; message id
                    0x00 0x05
                    ;; granted qos(es)
                    0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :suback (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 5 (:message-id decoded))))

      (testing "it parses the granted qos(es)"
        (is (= [0] (:granted-qos decoded))))))

  (testing "when parsing suback packet for two topics"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x90
                    ;; remaining length
                    0x04
                    ;; message id
                    0x00 0x06
                    ;; granted qos(es)
                    0x00
                    0x01)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :suback (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 6 (:message-id decoded))))

      (testing "it parses the granted qos(es)"
        (is (= [0 1] (:granted-qos decoded)))))))
