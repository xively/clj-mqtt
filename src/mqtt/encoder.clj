(ns mqtt.encoder
  (:use mqtt.core
        mqtt.packets.common
        mqtt.packets.connack
        mqtt.packets.connect
        mqtt.packets.disconnect
        mqtt.packets.pingreq
        mqtt.packets.pingresp
        mqtt.packets.puback
        mqtt.packets.pubcomp
        mqtt.packets.publish
        mqtt.packets.pubrec
        mqtt.packets.pubrel
        mqtt.packets.suback
        mqtt.packets.subscribe
        mqtt.packets.unsuback
        mqtt.packets.unsubscribe)
  (:import [io.netty.handler.codec MessageToByteEncoder]))

(defn- int-to-byte
  [i]
  (byte i))

(defn- bool-to-byte
  [bool]
  (byte (if bool 0x01 0x00)))

(defn encode-fixed-header
  [packet out]
  (let [type-byte (message-type-byte (:type packet))
        dup       (bool-to-byte (:dup packet))
        qos       (int-to-byte (or (:qos packet) 0))
        retain    (bool-to-byte (:retain packet))
        flags (bit-or (bit-shift-left type-byte 4)
                      (bit-shift-left dup 3)
                      (bit-shift-left qos 1)
                      (bit-shift-left retain 0))]
    (.writeByte out flags))
  packet)

(defn encode-remaining-length
  "Encode MQTT's funky multi-byte remaining-length field. It needs to use the
  algorithm from the MQTT 3.1 Spec:
  http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html

  do
    digit = X MOD 128
    X = X DIV 128
    // if there are more digits to encode, set the top bit of this digit
    if ( X > 0 )
      digit = digit OR 0x80
    endif
    'output' digit
  while ( X> 0 )
  "
  ([packet out] (encode-remaining-length packet out (remaining-length packet)))
  ([packet out x]
    (let [digit (mod x 128)
          x     (quot x 128)]
      (if (> x 0)
        (do
          (.writeByte out (bit-or digit 0x80))
          (recur packet out x))
        (do
          (.writeByte out digit)
          packet)))))

(defn make-encoder
  []
  (proxy [MessageToByteEncoder] []
    (encode [ctx msg out]
      (let [msg (merge (message-defaults msg) msg)]
        (validate-message msg)
        (encode-fixed-header msg out)
        (encode-remaining-length msg out)
        (encode-variable-header msg out)
        (encode-payload msg out)))))
