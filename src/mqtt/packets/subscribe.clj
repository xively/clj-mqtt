(ns mqtt.packets.subscribe
  (:use mqtt.decoding-utils
        mqtt.encoding-utils
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

(defmethod message-defaults :subscribe
  [message]
  {:qos 1})

(defmethod validate-message :subscribe
  [packet]
  packet)

(defmethod remaining-length :subscribe
  [{:keys [topics]}]
  (let [topic-length (fn [[topic qos]] (+ 3 (count (utf8-bytes topic))))]
    (+ 2 ;; message id
       ;; topics and subscriptions
       (reduce + (map topic-length topics)))))

(defmethod encode-variable-header :subscribe
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :subscribe
  [{:keys [topics] :as packet} out]
  (doseq [[topic qos] topics]
    (encode-string out topic)
    (encode-byte out qos))
  packet)
