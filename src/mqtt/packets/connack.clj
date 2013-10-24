(ns mqtt.packets.connack
  (:use mqtt.core
        mqtt.encoding-utils
        mqtt.decoding-utils
        mqtt.packets.common)
  (:import [io.netty.handler.codec EncoderException]))

(defmethod message-defaults :connack
  [message]
  {:return-code :accepted})

(defmethod validate-message :connack
  [message]
  (if-not (connack-return-code-byte (:return-code message))
    (throw (EncoderException.))))

(defmethod remaining-length :connack
  [packet]
  2)

(defmethod encode-variable-header :connack
  [packet out]
  (encode-byte out 0x00)
  (encode-byte out (connack-return-code-byte (:return-code packet))))

(defmethod encode-payload :connack
  [packet out]
  packet)

(defmethod decode-variable-header :connack
  [packet in]
  (parse-unsigned-byte in)
  (let [raw-return-code (parse-unsigned-byte in)]
    (assoc packet :return-code (or (connack-return-codes raw-return-code)
                                   raw-return-code))))

(defmethod decode-payload :connack
  [packet in]
  packet)
