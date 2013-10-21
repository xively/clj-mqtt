(ns mqtt.packets.unsuback-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.unsuback)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest unsuback-validate-message-test
  (testing "nil when valid"
    (is (= nil (validate-message {:type :unsuback
                                  :message-id 1}))))

  (testing "it throws if no message-id"
    (is (thrown? EncoderException (validate-message {:type :unsuback}))))

  (testing "it throws if message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :unsuback
                                                     :message-id 0})))))

(deftest unsuback-encoding-test
  (testing "when encoding a simple unsuback packet"
    (let [encoder (make-encoder)
          packet  {:type :unsuback :message-id 5}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xB0
                        ;; remaining length
                        2
                        ;; message id
                        0x00 0x05))))))

  (testing "when encoding a unsuback for two topics"
    (let [encoder (make-encoder)
          packet  {:type :unsuback :message-id 6}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xB0
                        ;; remaining length
                        2
                        ;; message id
                        0x00 0x06)))))))

(deftest decoding-unsuback-packet-test
  (testing "when parsing a simple unsuback packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0xB0
                    ;; remaining length
                    2
                    ;; message id
                    0x00 0x05)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :unsuback (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 5 (:message-id decoded))))))

  (testing "when parsing unsuback packet for two topics"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0xB0
                    ;; remaining length
                    2
                    ;; message id
                    0x00 0x06)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :unsuback (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 6 (:message-id decoded)))))))
