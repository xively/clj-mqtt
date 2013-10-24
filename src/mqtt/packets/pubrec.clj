(ns mqtt.packets.pubrec
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common))

(defmethod decode-variable-header :pubrec
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defmethod decode-payload :pubrec
  [packet in]
  packet)

(defmethod message-defaults :pubrec
  [message]
  {})

(defmethod validate-message :pubrec
  [{:keys [message-id]}]
  (validate-message-id 1 message-id))

(defmethod remaining-length :pubrec
  [packet]
  2)

(defmethod encode-variable-header :pubrec
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :pubrec
  [packet out]
  packet)
