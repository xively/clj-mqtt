(ns mqtt.packets.pingreq-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.decoder))

(deftest decoding-pingreq-packet-test
  (testing "when parsing a simple Pingreq packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0xC0 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :pingreq (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded)))))))
