(ns mqtt.packets.subscribe-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.decoder))

(deftest decoding-subscribe-packet-test
  (testing "when parsing a packet with a single topic"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer ;; fixed header
                                        0x82 0x08
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
        (is (= false (:dup decoded))))

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
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 1 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "it parses the message id"
        (is (= 6 (:message-id decoded))))

      (testing "parses the topics"
        (is (= [["a/b" 0] ["c/d" 1]] (:topics decoded)))))))
