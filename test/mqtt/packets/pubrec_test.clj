(ns mqtt.packets.pubrec-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.pubrec)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest pubrec-validate-message-test
  (testing "nil when valid"
    (is (= nil (validate-message {:type :pubrec
                                  :message-id 1}))))

  (testing "it throws if no message-id"
    (is (thrown? EncoderException (validate-message {:type :pubrec}))))

  (testing "it throws if message-id is 0"
    (is (thrown? EncoderException (validate-message {:type :pubrec
                                                     :message-id 0})))))

(deftest pubrec-encoding-test
  (testing "when encoding a simple pubrec packet"
    (let [encoder (make-encoder)
          packet  {:type :pubrec :message-id 5}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out)
             [;; fixed header
              2r01010000
              ;; remaining length
              2
              ;; message id
              0 5])))))

(deftest decoding-pubrec-packet-test
  (testing "when parsing a simple pubrec packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    2r01010000
                    ;; remaining length
                    2
                    ;; message id
                    0 5)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :pubrec (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 5 (:message-id decoded)))))))
