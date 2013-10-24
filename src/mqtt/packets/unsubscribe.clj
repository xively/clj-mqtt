(ns mqtt.packets.unsubscribe
  (:use mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:import [io.netty.buffer ByteBuf]))

(defmethod decode-variable-header :unsubscribe
  [packet in]
  (assoc packet :message-id (parse-unsigned-short in)))

(defn- parse-topics
  [^ByteBuf in]
  (loop [topics []]
    (if (.isReadable in)
      (recur (conj topics (parse-string in)))
      topics)))

(defmethod decode-payload :unsubscribe
  [packet in]
  (assoc packet :topics (parse-topics in)))

(defmethod message-defaults :unsubscribe
  [message]
  {:qos 1})

(defmethod validate-message :unsubscribe
  [packet]
  packet)

(defmethod remaining-length :unsubscribe
  [{:keys [topics]}]
  (let [topic-length (fn [topic] (+ 2 (count (utf8-bytes topic))))]
    (+ 2 ;; message id
       ;; topics and subscriptions
       (reduce + (map topic-length topics)))))

(defmethod encode-variable-header :unsubscribe
  [{:keys [message-id] :as packet} out]
  (encode-unsigned-short out message-id)
  packet)

(defmethod encode-payload :unsubscribe
  [{:keys [topics] :as packet} out]
  (doseq [topic topics]
    (encode-string out topic))
  packet)
