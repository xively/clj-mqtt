(ns mqtt.decoder
  (:use mqtt.core
        mqtt.decoding-utils
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
  (:import [io.netty.handler.codec ReplayingDecoder]
           [java.io StreamCorruptedException]))

(defn decode-fixed-header
  "Decode the first byte of a packet. Checks that message-type is not nil."
  ([in] (decode-fixed-header {} in))
  ([packet in]
    (let [flags (parse-flags in
                            :type 4
                            :dup 1
                            :qos 2
                            :retain 1)
          parsed-message-type (message-types (:type flags))]

      (when (nil? parsed-message-type)
        (throw (new StreamCorruptedException "Valid message types are 1 through 14")))

      {:type    parsed-message-type
       :dup     (:dup flags)
       :qos     (:qos flags)
       :retain  (:retain flags)})))

(defn parse-remaining-length
  "Decode the remaining length field of a packet. This can be more than one
  byte long. Algorithm from the MQTT 3.1 Spec:
  http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html
  under the 'Remaining Length' section.

  multiplier = 1
  value = 0
  do
    digit = 'next digit from stream'
    value += (digit AND 127) * multiplier
    multiplier *= 128
  while ((digit AND 128) != 0)
  "
  ([in] (parse-remaining-length in 1 0))
  ([in multiplier value]
    (let [digit (.readUnsignedByte in)
          value (+ value (* (bit-and 127 digit) multiplier))
          multiplier (* multiplier 128)]
      (if (zero? (bit-and 128 digit))
        value
        (recur in multiplier value)))))

(defn make-decoder
  []
  (proxy [ReplayingDecoder] []
    (decode [ctx in out]
      (when (.isReadable in)
        (let [fixed-header     (decode-fixed-header in)
              ;; slice down the buffer so we can't overflow
              new-in           (.readSlice in (parse-remaining-length in))]
          (.add out (-> fixed-header (decode-variable-header new-in) (decode-payload new-in))))))))
