(ns mqtt.packets.connack-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.encoder
        mqtt.decoder
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

(deftest connack-decoding-test
  (testing "when parsing a successful Connection Accepted packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed Header
                    0x20
                    ;; Remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code: accepted
                    0x00)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :accepted (:return-code decoded))))))

  (testing "when parsing a unacceptable protocol version packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed Header
                    0x20
                    ;; Remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code
                    0x01)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :unacceptable-protocol-version (:return-code decoded))))))

  (testing "when parsing a client identifier rejected packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed Header
                    0x20
                    ;; Remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code
                    0x02)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :identifier-rejected (:return-code decoded))))))

  (testing "when parsing a broker unavailable packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed Header
                    0x20
                    ;; Remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code
                    0x03)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :server-unavailable (:return-code decoded))))))

  (testing "when parsing bad username or password packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x20
                    ;; remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code
                    0x04)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :bad-username-or-password (:return-code decoded))))))

  (testing "when parsing unauthorized packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x20
                    ;; remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code
                    0x05)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= :unauthorized (:return-code decoded))))))

  (testing "when parsing unknown response code packet" 
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; fixed header
                    0x20
                    ;; remaining length
                    0x02
                    ;; reserved (unused)
                    0x00
                    ;; return-code: unknown
                    0x10)
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connack (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "should set the return code"
        (is (= 0x10 (:return-code decoded)))))))
