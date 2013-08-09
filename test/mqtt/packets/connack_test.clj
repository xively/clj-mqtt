(ns mqtt.packets.connack-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.connack)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest connack-validate-message-test
  (testing "return-code when valid"
    (is (= nil (validate-message {:type :connack :return-code :accepted}))))

  (testing "it throws for blank return-code"
    (is (thrown? EncoderException (validate-message {:type :connack}))))

  (testing "it throws for invalid return-code"
    (is (thrown? EncoderException (validate-message {:type :connack :return-code :bad-code})))))

(deftest connack-encoding-test
  (testing "when encoding a simple Connack packet"
    (let [encoder (make-encoder)
          packet  {:type :connack :return-code :accepted}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              0x20
              ;; remaining length
              0x02
              ;; reserved (unused)
              0x00
              ;; return-code: accepted
              0x00]))))

  (testing "when encoding a Connack packet with everything set"
    (let [encoder (make-encoder)
          packet  {:type :connack :return-code :accepted :dup true :qos 3 :retain true}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              0x2F
              ;; remaining length
              0x02
              ;; reserved (unused)
              0x00
              ;; return-code: accepted
              0x00]))))

  (testing "when encoding a rejected Connack packet"
    (let [encoder (make-encoder)
          packet  {:type :connack :return-code :bad-username-or-password}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              0x20
              ;; remaining length
              0x02
              ;; reserved (unused)
              0x00
              ;; return-code: bad username or password
              0x04]))))

  (testing "when encoding a Connack packet without a return code"
    (let [encoder (make-encoder)
          packet  {:type :connack}
          out     (Unpooled/buffer 4)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              0x20
              ;; remaining length
              0x02
              ;; reserved (unused)
              0x00
              ;; return-code: default is accepted
              0x00]))))

  (testing "when encoding a Connack packet with an invalid return code"
    (let [encoder (make-encoder)
          packet  {:type :connack :return-code :not-a-valid-return-code}
          out     (Unpooled/buffer 4)]
      (is (thrown? EncoderException (.encode encoder nil packet out))))))

