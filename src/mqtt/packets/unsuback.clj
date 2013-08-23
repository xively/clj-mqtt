(ns mqtt.packets.unsuback
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.handler.codec EncoderException]))

(defmethod decode-variable-header :unsuback
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defmethod decode-payload :unsuback
  [packet in]
  packet)

(defmethod message-defaults :unsuback
  [message]
  {})

(defmethod validate-message :unsuback
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :unsuback
  [{:keys [granted-qos]}]
  ;; message id
  2)

(defmethod encode-variable-header :unsuback
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :unsuback
  [packet out]
  packet)
