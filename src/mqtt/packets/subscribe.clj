(ns mqtt.packets.subscribe
  (:use mqtt.decoding-utils
        mqtt.packets.common))

(defmethod decode-variable-header :subscribe
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defn- parse-topics
  [in]
  (loop [topics []]
    (if (.isReadable in)
      (recur (conj topics [(parse-string in) (parse-unsigned-byte in)]))
      topics)))

(defmethod decode-payload :subscribe
  [packet in]
  (assoc packet :topics (parse-topics in)))
