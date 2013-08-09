(ns mqtt.packets.connect
  (:use mqtt.decoding-utils
        mqtt.packets.common))

(defn- decode-connect-flags
  [packet in]
  (merge packet (parse-flags in
                             :has-username 1
                             :has-password 1
                             :will-retain 1
                             :will-qos 2
                             :has-will 1
                             :clean-session 1)))

(defmethod decode-variable-header :connect
  [packet in]
  (-> packet
      (assoc :protocol-name    (parse-string in)
             :protocol-version (parse-unsigned-byte in))
      (decode-connect-flags in)
      (assoc :keepalive (parse-unsigned-short in))))

(defn- decode-client-id
  [packet in]
  (assoc packet :client-id (parse-string in)))

(defn- decode-will-topic
  [{:keys [has-will] :as packet} in]
  (if has-will
    (assoc packet :will-topic (parse-string in))
    packet))

(defn- decode-will-payload
  [{:keys [has-will] :as packet} in]
  (if has-will
    (assoc packet :will-payload (parse-string in))
    packet))

(defn- decode-username
  [{:keys [has-username] :as packet} in]
  (if has-username
    (assoc packet :username (parse-string in))
    packet))

(defn- decode-password
  [{:keys [has-password] :as packet} in]
  (if has-password
    (assoc packet :password (parse-string in))
    packet))

(defmethod decode-payload :connect
  [packet in]
  (-> packet
      (decode-client-id in)
      (decode-will-topic in)
      (decode-will-payload in)
      (decode-username in)
      (decode-password in)))
