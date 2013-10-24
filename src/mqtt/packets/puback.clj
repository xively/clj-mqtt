(ns mqtt.packets.puback
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common))

(defmethod decode-variable-header :puback
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defmethod decode-payload :puback
  [packet in]
  packet)

(defmethod message-defaults :puback
  [message]
  {})

(defmethod validate-message :puback
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :puback
  [packet]
  2)

(defmethod encode-variable-header :puback
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :puback
  [packet out]
  packet)
