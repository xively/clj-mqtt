(ns mqtt.packets.pubrel-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.pubrel)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest pubrel-validate-message-test
  (testing "nil when valid"
    (is (= nil (validate-message {:type :pubrel
                                  :message-id 1}))))

  (testing "it throws if no message-id"
    (is (thrown? EncoderException (validate-message {:type :pubrel}))))

  (testing "it throws if message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :pubrel
                                                     :message-id 0})))))

(deftest pubrel-encoding-test
  (testing "when encoding a simple pubrel packet"
    (let [encoder (make-encoder)
          packet  {:type :pubrel :message-id 5}
          out     (Unpooled/buffer 4)]
      (.encode encoder nil packet out)
      (is (= (byte-buffer-to-bytes out)
             [;; fixed header
              2r01100010
              ;; remaining length
              2
              ;; message id
              0 5])))))

(deftest decoding-pubrel-packet-test
  (testing "when parsing a simple pubrel packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    2r01100010
                    ;; remaining length
                    2
                    ;; message id
                    0 5)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :pubrel (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 5 (:message-id decoded)))))))
