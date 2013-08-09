(ns mqtt.packets.suback
  (:use mqtt.core
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.handler.codec EncoderException]))

(defmethod message-defaults :suback
  [message]
  message)

(defmethod validate-message :suback
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :suback
  [{:keys [granted-qos]}]
  (+ 2 (count granted-qos)))

(defmethod encode-variable-header :suback
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :suback
  [{:keys [granted-qos] :as packet} out]
  (doseq [qos granted-qos]
    (encode-byte out qos))
  packet)
