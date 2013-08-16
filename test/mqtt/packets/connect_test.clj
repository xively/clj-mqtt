(ns mqtt.packets.connect-test
  (:use clojure.test
        mqtt.test_helpers
        mqtt.encoder
        mqtt.decoder
        mqtt.packets.common
        mqtt.packets.connack)
  (:import [io.netty.buffer Unpooled]
           [io.netty.handler.codec EncoderException]))

(deftest connect-validate-message-test
  (testing "returns nil when valid"
    (is (= nil (validate-message {:type :connect :client-id "1"}))))

  (testing "it throws for no client id"
    (is (thrown? EncoderException (validate-message {:type :connect}))))

  (testing "it throws for blank client id"
    (is (thrown? EncoderException (validate-message {:type :connect :client-id ""}))))

  (testing "it throws for password but no username"
    (is (thrown? EncoderException (validate-message {:type :connect :client-id "1" :password "pa55word"}))))

  (testing "it throws for negative keepalive"
    (is (thrown? EncoderException (validate-message {:type :connect :client-id "1" :keepalive -1})))))

(deftest encoding-connect-packet-test
  (testing "when encoding a simple Connect packet"
    (let [encoder (make-encoder)
          packet  {:type :connect
                   :client-id "myclient"
                   :keepalive 0x0a}
          out     (Unpooled/buffer 24)
          _       (.encode encoder nil packet out)]
      (is (= (byte-buffer-to-bytes out) 
             [;; fixed header
              0x10
              ;; remaining length
              23
              ;; Protocol name
              0x00 0x06 "MQIsdp"
              ;; protocol version
              0x03
              ;; connect flags
              0x00
              ;; keepalive
              0x00 0x0a
              ;; client id
              0x00 0x08 "myclient"])))))

(deftest decoding-connect-packet-test
  (testing "when parsing a simple Connect packet"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer 0x10 0x16 0x00 0x06 "MQIsdp" 0x03 0x00 0x00 0x0a 0x00 0x08 "myclient")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connect (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the protocol version"
        (is (= 3 (:protocol-version decoded))))

      (testing "parses the protocol name"
        (is (= "MQIsdp" (:protocol-name decoded))))

      (testing "parses the client id"
        (is (= "myclient" (:client-id decoded))))

      (testing "parses the keepalive timer"
        (is (= 10 (:keepalive decoded))))

      (testing "should not have clean session set"
        (is (= false (:clean-session decoded))))

      (testing "should not have a username"
        (is (nil? (:username decoded))))

      (testing "should not have a password"
        (is (nil? (:password decoded))))))

  (testing "when parsing a Connect packet with the clean session flag set"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x16
                    ;; Variable header
                    0x00 0x06 "MQIsdp" 0x03 0x02 0x00 0x0a
                    ;; Payload
                    0x00 0x08 "myclient")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "should have clean session set"
        (is (= true (:clean-session decoded))))))

  (testing "when parsing a Connect packet with a Will and Testament"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x24
                    ;; Variable header
                    0x00 0x06 "MQIsdp" 0x03 0x0e 0x00 0x0a
                    ;; Payload
                    0x00 0x08 "myclient"
                    0x00 0x05 "topic"
                    0x00 0x05 "hello")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connect (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the protocol version"
        (is (= 3 (:protocol-version decoded))))

      (testing "parses the protocol name"
        (is (= "MQIsdp" (:protocol-name decoded))))

      (testing "parses the client id"
        (is (= "myclient" (:client-id decoded))))

      (testing "should not have clean session set"
        (is (= true (:clean-session decoded))))

      (testing "parses the keepalive timer"
        (is (= 10 (:keepalive decoded))))

      (testing "should not have a username"
        (is (nil? (:username decoded))))

      (testing "should not have a password"
        (is (nil? (:password decoded))))

      (testing "should set the QOS of the will packet"
        (is (= 1 (:will-qos decoded))))

      (testing "should set the retain flag of the will packet"
        (is (= false (:will-retain decoded))))

      (testing "should set the topic of the will packet"
        (is (= "topic" (:will-topic decoded))))

      (testing "should set the payload of the will packet"
        (is (= "hello" (:will-payload decoded))))))

  (testing "when parsing a Connect packet with a username and password"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x2A
                    ;; Variable header
                    0x00 0x06 "MQIsdp" 0x03 0xC0 0x00 0x0a
                    ;; Payload
                    0x00 0x08 "myclient"
                    0x00 0x08 "username"
                    0x00 0x08 "password")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connect (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the protocol version"
        (is (= 3 (:protocol-version decoded))))

      (testing "parses the protocol name"
        (is (= "MQIsdp" (:protocol-name decoded))))

      (testing "parses the client id"
        (is (= "myclient" (:client-id decoded))))

      (testing "should not have clean session set"
        (is (= false (:clean-session decoded))))

      (testing "parses the keepalive timer"
        (is (= 10 (:keepalive decoded))))

      (testing "should set the username of the packet correctly"
        (is (= "username" (:username decoded))))

      (testing "should set the password of the packet correctly"
        (is (= "password" (:password decoded))))))

  (testing "when parsing a Connect packet with a username but no password"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x20
                    ;; Variable header
                    0x00 0x06 "MQIsdp" 0x03 0x80 0x00 0x0a
                    ;; Payload
                    0x00 0x08 "myclient"
                    0x00 0x08 "username")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "should set the username of the packet correctly"
        (is (= "username" (:username decoded))))

      (testing "should not set the password of the packet"
        (is (nil? (:password decoded))))))

  (testing "when parsing a Connect packet with a password but no username"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x20
                    ;; Variable header
                    0x00 0x06 "MQIsdp" 0x03 0x40 0x00 0x0a
                    ;; Payload
                    0x00 0x08 "myclient"
                    0x00 0x08 "password")
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "should set the password of the packet correctly"
        (is (= "password" (:password decoded))))

      (testing "should not set the username of the packet"
        (is (nil? (:username decoded))))))

  (testing "when parsing a Connect packet with the username and password flags set but doesn't have the fields"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header
                    0x10 0x16
                    ;; protocol name
                    0x00 0x06 "MQIsdp"
                    ;; protocol version + flags
                    0x03 0xC0
                    ;; keepalive
                    0x00 0x0a
                    ;; client id
                    0x00 0x08 "myclient")
          out     (new java.util.ArrayList)]

      (testing "it throws an exception because this is not valid"
        (is (thrown? java.io.StreamCorruptedException
          (.decode decoder nil packet out))))))

  (testing "when parsing a Connect packet with every option set"
    (let [decoder (make-decoder)
          packet  (bytes-to-byte-buffer
                    ;; Fixed header             ( 2 bytes)
                    0x10 0x5F
                    ;; Protocol Name            ( 8 bytes)
                    0x00 0x06 "MQIsdp" 
                    ;; Protocol version + flags ( 2 bytes)
                    0x03 0xF6
                    ;; Keepalive                ( 2 bytes)
                    0xFF 0xFF
                    ;; Client Id                (25 bytes)
                    0x00 0x17 "12345678901234567890123"
                    ;; will topic               (12 bytes)
                    0x00 0x0A "will_topic"
                    ;; will message             (14 bytes)
                    0x00 0x0C "will_message"
                    ;; username                 (16 bytes)
                    0x00 0x0E "user0123456789"
                    ;; password                 (16 bytes)
                    0x00 0x0E "pass0123456789"
                    )
          out     (new java.util.ArrayList)
          _       (.decode decoder nil packet out)
          decoded (first out)]

      (testing "parses a packet"
        (is (not (nil? decoded)))
        (is (= :connect (:type decoded))))

      (testing "should not be a duplicate"
        (is (= false (:dup decoded))))

      (testing "parses the qos"
        (is (= 0 (:qos decoded))))

      (testing "should not be retained"
        (is (= false (:retain decoded))))

      (testing "parses the protocol version"
        (is (= 3 (:protocol-version decoded))))

      (testing "parses the protocol name"
        (is (= "MQIsdp" (:protocol-name decoded))))

      (testing "parses the client id"
        (is (= "12345678901234567890123" (:client-id decoded))))

      (testing "parses the keepalive timer"
        (is (= 65535 (:keepalive decoded))))

      (testing "should not have clean session set"
        (is (= true (:clean-session decoded))))

      (testing "should set the will topic"
        (is (= "will_topic" (:will-topic decoded))))

      (testing "should set the will payload"
        (is (= "will_message" (:will-payload decoded))))

      (testing "should have a username"
        (is (= "user0123456789" (:username decoded))))

      (testing "should have a password"
        (is (= "pass0123456789" (:password decoded)))))))
