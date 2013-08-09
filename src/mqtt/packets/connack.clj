(ns mqtt.packets.connack
  (:use mqtt.core
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.handler.codec EncoderException]))

(defmethod message-defaults :connack
  [message]
  {:return-code :accepted})

(defmethod validate-message :connack
  [message]
  (if-not (connack-return-code-byte (:return-code message))
    (throw (new EncoderException))))

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
