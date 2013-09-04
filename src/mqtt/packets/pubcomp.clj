(ns mqtt.packets.pubcomp
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.handler.codec EncoderException]))

(defmethod decode-variable-header :pubcomp
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defmethod decode-payload :pubcomp
  [packet in]
  packet)

(defmethod message-defaults :pubcomp
  [message]
  {})

(defmethod validate-message :pubcomp
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :pubcomp
  [packet]
  2)

(defmethod encode-variable-header :pubcomp
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :pubcomp
  [packet out]
  packet)
