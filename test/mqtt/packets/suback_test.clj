(ns mqtt.packets.suback-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.suback)
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
          out     (Unpooled/buffer 5)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              (unsigned-byte 0x90)
              ;; remaining length
              0x03
              ;; message id
              0x00
              0x05
              ;; granted qos(es)
              0x00]))))

  (testing "when encoding a suback for two topics"
    (let [encoder (make-encoder)
          packet  {:type :suback :message-id 6 :granted-qos [0 1]}
          out     (Unpooled/buffer 5)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              (unsigned-byte 0x90)
              ;; remaining length
              0x04
              ;; message id
              0x00
              0x06
              ;; granted qos(es)
              0x00
              0x01])))))
