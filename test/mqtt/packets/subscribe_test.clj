(ns mqtt.packets.subscribe-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.subscribe)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest subscribe-validate-message-test
  (testing "nil when valid"
    (is (= nil (validate-message {:type :subscribe
                                  :message-id 1}))))

  (testing "it throws if no message-id"
    (is (thrown? EncoderException (validate-message {:type :subscribe}))))

  (testing "it throws if message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :subscribe
                                                     :message-id 0})))))

(deftest encoding-subscribe-packet-test
  (testing "when encoding a simple subscribe packet"
    (let [encoder (make-encoder)
          packet  {:type :subscribe
                   :message-id 1
                   :topics [["a/b" 0]]}
          out     (Unpooled/buffer 10)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x82
                        ;; remaining length
                        0x08
                        ;; message id
                        0x00 0x01
                        ;; topic + qos
                        0x00 0x03 "a/b" 0x00))))))

  (testing "when encoding a subscribe packet with two packets"
    (let [encoder (make-encoder)
          packet  {:type :subscribe
                   :message-id 5
                   :topics [["a/b" 0] ["c/d" 1]]}
          out     (Unpooled/buffer 16)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0x82
                        ;; remaining length
                        14
                        ;; message id
                        0x00 0x05
                        ;; topic + qos
                        0x00 0x03 "a/b" 0x00
                        0x00 0x03 "c/d" 0x01)))))))

(deftest decoding-subscribe-packet-test
  (testing "when parsing a packet with a single topic"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer ;; fixed header
                                        0x82
                                        ;; remaining length
                                        0x08
                                        ;; message id
                                        0x00 0x01
                                        ;; topic + qos
                                        0x00 0x03 "a/b" 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :subscribe (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 1 (:message-id decoded))))

      (testing "parses the topics"
        (is (= [["a/b" 0]] (:topics decoded))))))

  (testing "when parsing a packet with two topics"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer ;; fixed header
                                        0x82 0x0e
                                        ;; message id
                                        0x00 0x06
                                        ;; topic + qos
                                        0x00 0x03 "a/b" 0x00
                                        0x00 0x03 "c/d" 0x01)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :subscribe (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:duplicate decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 6 (:message-id decoded))))

      (testing "parses the topics"
        (is (= [["a/b" 0] ["c/d" 1]] (:topics decoded)))))))
