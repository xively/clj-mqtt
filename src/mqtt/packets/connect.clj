(ns mqtt.packets.connect
  (:use mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:require [clojure.string :as string])
  (:import [io.netty.handler.codec EncoderException]))

(defmethod validate-message :connect
  [{:keys [username password keepalive client-id]}]
  (if (string/blank? client-id)
    (throw (new EncoderException)))

  (if (and (string/blank? username)
           (not (string/blank? password)))
    (throw (new EncoderException)))

  (if (and (not (nil? keepalive))
           (neg? keepalive))
    (throw (new EncoderException))))

(defmethod message-defaults :connect
  [message]
  {:protocol-version 0x03
   :protocol-name "MQIsdp"
   :clean-session true
   :will-retain false
   :will-qos 1
   :keepalive 10})

(defmethod remaining-length :connect
  [{:keys [client-id] :as packet}]
  (+ 1 ;; protocol version
     1 ;; flags
     2 ;; keepalive
     2 (count (utf8-bytes client-id)))) ;; client-id length

(defmethod encode-variable-header :connect
  [{:keys [protocol-version protocol-name keepalive] :as packet} out]
  (encode-string out protocol-name)
  (encode-byte out protocol-version)
  packet)

(defmethod encode-payload :connect
  [{:keys [client-id] :as packet} out]
  (encode-string out client-id)
  packet)

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
