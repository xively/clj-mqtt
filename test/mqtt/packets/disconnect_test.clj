(ns mqtt.packets.disconnect-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.decoder))

(deftest decoding-disconnect-packet-test
  (testing "when parsing a simple disconnect packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0xE0 0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :disconnect (:type decoded)))))))
