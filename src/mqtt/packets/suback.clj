(ns mqtt.packets.suback
  (:use mqtt.core
        mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.buffer ByteBuf]))

(defmethod decode-variable-header :suback
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defn- parse-granted-qos
  [^ByteBuf in]
  (loop [granted-qos []]
    (if (.isReadable in)
      (recur (conj granted-qos (parse-unsigned-byte in)))
      granted-qos)))

(defmethod decode-payload :suback
  [packet in]
  (assoc packet :granted-qos (parse-granted-qos in)))

(defmethod message-defaults :suback
  [message]
  {})

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
