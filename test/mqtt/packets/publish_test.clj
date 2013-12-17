(ns mqtt.packets.publish-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.publish)
  (:require [clojure.string :as string])
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest decoding-publish-packet-test
  (testing "when parsing a simple publish packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x30 0x11
                    ;; topic
                    0x00 0x04 "test"
                    ;; payload
                    "hello world")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :publish (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the topic name"
        (is (= "test" (:topic decoded))))

      (testing "payload is array of bytes"
        (let [barray (byte-array 0)]
              (is (= (class barray) (class (:payload decoded))))))

      (testing "parses the payload"
        (is (= "hello world" (String. (:payload decoded)))))))

  (testing "when parsing a publish packet with qos 2 and retain and dup flags set"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x3D
                    ;; remaining length
                    0x12
                    ;; topic
                    0x00 0x03 "c/d"
                    ;; message-id
                    0x00 0x05
                    ;; payload
                    "hello world")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :publish (:type decoded))))

      (testing "should not be a duplicate"
        (is (= true (:dup decoded))))

      (testing "parses the qos"
        (is (= 2 (:qos decoded))))

      (testing "should not be retained"
        (is (= true (:retain decoded))))

      (testing "parses the topic name"
        (is (= "c/d" (:topic decoded))))

      (testing "payload is array of bytes"
        (let [barray (byte-array 0)]
              (is (= (class barray) (class (:payload decoded))))))

      (testing "parses the payload"
        (is (= "hello world" (String. (:payload decoded)))))))

  (testing "when parsing a publish packet with a 314 byte body"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x30
                    ;; remaining length
                    0xC1 0x02
                    ;; topic
                    0x00 0x05 "topic"
                    ;; payload
                    (string/join (take 314 (repeat \x))))
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :publish (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the topic name"
        (is (= "topic" (:topic decoded))))

      (testing "parses the protocol name"
        (is (= 314 (count (:payload decoded)))))))

  (testing "when parsing a publish packet with a 16kilobyte body"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x30
                    ;; remaining length
                    0x87 0x80 0x01
                    ;; topic
                    0x00 0x05 "topic"
                    ;; payload
                    (string/join (take 16384 (repeat \x))))
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :publish (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the topic name"
        (is (= "topic" (:topic decoded))))

      (testing "parses the protocol name"
        (is (= 16384 (count (:payload decoded))))))))

(deftest publish-validate-message-test
  (testing "returns nil when valid"
    (is (= nil (validate-message {:type :publish :topic "test" :payload "ohai"}))))

  (testing "returns nil when valid with qos1"
    (is (= nil (validate-message {:type :publish
                                  :qos 1
                                  :message-id 1
                                  :topic "test"
                                  :payload "ohai"}))))

  (testing "it throws for blank topic"
    (is (thrown? EncoderException (validate-message {:type :publish
                                                     :payload "ohai"}))))

  (testing "it throws if qos > 0 and no message-id"
    (is (thrown? EncoderException (validate-message {:type :publish
                                                     :qos 1
                                                     :topic "test"
                                                     :payload "ohai"}))))

  (testing "it throws if qos > 0 and message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :publish
                                                     :qos 1
                                                     :message-id 0
                                                     :topic "test"
                                                     :payload "ohai"})))))

(deftest encoding-publish-packet-test
  (testing "when encoding a simple publish packet"
    (let [encoder (make-encoder)
          packet  {:type :publish :topic "test" :payload "hello world"}
          out     (Unpooled/buffer 19)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x30
                        ;; remaining length
                        0x11
                        ;; topic
                        0x00 0x04 "test"
                        ;; payload
                        "hello world"))))))

  (testing "when encoding a publish packet with no payload"
    (let [encoder (make-encoder)
          packet  {:type :publish :topic "test"}
          out     (Unpooled/buffer 8)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x30
                        ;; remaining length
                        0x06
                        ;; topic
                        0x00 0x04 "test"))))))

  (testing "when encoding a publish packets with other types of payloads"
    (doseq [[container  payload] {"string" "hello world"
                                  "byte-array" (bytes-to-byte-array "hello world")
                                  "io.netty.buffer.ByteBuf" (bytes-to-byte-buffer "hello world")
                                  "java.nio.ByteBuffer" (bytes-to-java-byte-buffer "hello world")}]
      (testing (str "data in a " container)
        (let [encoder (make-encoder)
              packet  {:type :publish :topic "test" :payload payload}
              out     (Unpooled/buffer 19)]
          (.encode encoder nil packet out)
          (is (= (byte-buffer-to-bytes out)
                 (into [] (bytes-to-byte-array
                           ;; fixed header
                           0x30
                           ;; remaining length
                           0x11
                           ;; topic
                           0x00 0x04 "test"
                           ;; payload
                           "hello world"))))))))

  (testing "when encoding a publish packet with qos 1 and message id"
    (let [encoder (make-encoder)
          packet  {:type :publish
                   :qos 1
                   :message-id 5
                   :topic "a/b"
                   :payload "hello world"}
          out     (Unpooled/buffer 0x13)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x32
                        ;; remaining length
                        0x12
                        ;; topic
                        0x00 0x03 "a/b"
                        ;; message id
                        0x00 0x05
                        ;; payload
                        "hello world"))))))

  (testing "when encoding a publish packet with qos 2 and retain flag set"
    (let [encoder (make-encoder)
          packet  {:type :publish
                   :qos 2
                   :retain true
                   :message-id 5
                   :topic "c/d"
                   :payload "hello world"}
          out     (Unpooled/buffer 0x13)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x35
                        ;; remaining length
                        0x12
                        ;; topic
                        0x00 0x03 "c/d"
                        ;; message id
                        0x00 0x05
                        ;; payload
                        "hello world"))))))

  (testing "when encoding a publish packet with qos 2 and dup flag set"
    (let [encoder (make-encoder)
          packet  {:type :publish
                   :qos 2
                   :dup true
                   :message-id 5
                   :topic "c/d"
                   :payload "hello world"}
          out     (Unpooled/buffer 0x13)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x3C
                        ;; remaining length
                        0x12
                        ;; topic
                        0x00 0x03 "c/d"
                        ;; message id
                        0x00 0x05
                        ;; payload
                        "hello world"))))))

  (testing "when encoding a publish packet with multibyte utf-8 characters"
    (let [encoder (make-encoder)
          packet  {:type :publish
                   :qos 0
                   :topic "currency.€"
                   :payload "23€"}
          out     (Unpooled/buffer)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x30
                        ;; remaining length
                        19
                        ;; topic
                        0x00 0x0C "currency.€"
                        ;; payload
                        "23€"))))))

  (testing "when encoding a publish packet with a 314 byte body"
    (let [encoder (make-encoder)
          packet  {:type :publish
                   :qos 0
                   :topic "topic"
                   :payload (string/join (take 314 (repeat \x)))}
          out     (Unpooled/buffer)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x30
                        ;; remaining length
                        0xC1 0x02
                        ;; topic
                        0x00 0x05 "topic"
                        ;; payload
                        (string/join (take 314 (repeat \x))))))))))
