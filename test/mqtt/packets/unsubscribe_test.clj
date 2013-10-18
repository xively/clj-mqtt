(ns mqtt.packets.unsubscribe-test
  (:use clojure.test
        mqtt.test-helpers
        mqtt.decoder
        mqtt.encoder
        mqtt.packets.common
        mqtt.packets.unsubscribe)
  (:import [io.netty.buffer Unpooled]))

(deftest unsubscribe-validate-message-test
  (testing "returns when valid"
    (let [packet {:type :unsubscribe}]
      (is (= packet (validate-message packet))))))

(deftest encoding-unsubscribe-packet-test
  (testing "when encoding a simple unsubscribe packet"
    (let [encoder (make-encoder)
          packet  {:type :unsubscribe
                   :message-id 1
                   :topics ["a/b"]}
          out     (Unpooled/buffer 9)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xA2
                        ;; remaining length
                        0x07
                        ;; message id
                        0x00 0x01
                        ;; topic
                        0x00 0x03 "a/b"))))))

  (testing "when encoding an unsubscribe packet with two packets"
    (let [encoder (make-encoder)
          packet  {:type :unsubscribe
                   :message-id 5
                   :topics ["a/b", "c/d"]}
          out     (Unpooled/buffer 14)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             (into [] (bytes-to-byte-array
                        ;; fixed header
                        0xA2
                        ;; remaining length
                        12
                        ;; message id
                        0x00 0x05
                        ;; topics
                        0x00 0x03 "a/b"
                        0x00 0x03 "c/d")))))))

(deftest decoding-unsubscribe-packet-test
  (testing "when parsing a packet with a single topic"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer ;; fixed header
                                        0xA2
                                        ;; remaining length
                                        0x07
                                        ;; message id
                                        0x00 0x01
                                        ;; topic
                                        0x00 0x03 "a/b")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :unsubscribe (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 1 (:message-id decoded))))

      (testing "parses the topics"
        (is (= ["a/b"] (:topics decoded))))))

  (testing "when parsing a packet with two topics"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer ;; fixed header
                                        0xA2
                                        ;; remaining length
                                        12
                                        ;; message id
                                        0x00 0x06
                                        ;; topic
                                        0x00 0x03 "a/b"
                                        0x00 0x03 "c/d")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :unsubscribe (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 6 (:message-id decoded))))

      (testing "parses the topics"
        (is (= ["a/b", "c/d"] (:topics decoded)))))))
