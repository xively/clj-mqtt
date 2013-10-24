(ns mqtt.packets.pubrel
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common))

(defmethod decode-variable-header :pubrel
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defmethod decode-payload :pubrel
  [packet in]
  packet)

(defmethod message-defaults :pubrel
  [message]
  {:qos 1})

(defmethod validate-message :pubrel
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :pubrel
  [packet]
  2)

(defmethod encode-variable-header :pubrel
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :pubrel
  [packet out]
  packet)
