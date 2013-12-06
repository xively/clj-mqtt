(ns mqtt.packets.connect
  (:use mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:require [clojure.string :as string])
  (:import [io.netty.handler.codec EncoderException]))

(defmethod validate-message :connect
  [{:keys [username password keepalive client-id]}]
  (if (string/blank? client-id)
    (throw (EncoderException.)))

  (if (and (string/blank? username)
           (not (string/blank? password)))
    (throw (EncoderException.)))

  (if (and (not (nil? keepalive))
           (neg? keepalive))
    (throw (EncoderException.))))

(defmethod message-defaults :connect
  [message]
  {:protocol-version 0x03
   :protocol-name "MQIsdp"
   :clean-session true
   :will-retain false
   :will-qos 0
   :keepalive 10})

(defn- optional-string-length
  [string]
  (if string
    (+ 2 (count (utf8-bytes string)))
    0))

(defmethod remaining-length :connect
  [{:keys [client-id protocol-name will-topic will-payload username password]}]
  (+ 2 (count (utf8-bytes protocol-name)) ;; protocol version
     1 ;; protocol version
     1 ;; flags
     2 ;; keepalive
     2 (count (utf8-bytes client-id)) ;; client-id length
     (optional-string-length will-topic)
     (optional-string-length will-payload)
     (optional-string-length username)
     (optional-string-length password)))

(defn flags
  [{:keys [username password will-retain will-qos will-topic clean-session]}]
  (cond-> 0x00
    username      (bit-set 7)
    password      (bit-set 6)
    will-retain   (bit-set 5)
    will-qos      (bit-or  (bit-shift-left (bit-and will-qos 0x03) 3))
    will-topic    (bit-set 2)
    clean-session (bit-set 1)))

(defmethod encode-variable-header :connect
  [{:keys [protocol-version protocol-name keepalive] :as packet} out]
  (encode-string out protocol-name)
  (encode-byte out protocol-version)
  (encode-byte out (flags packet))
  (encode-unsigned-short out keepalive)
  packet)

(defmethod encode-payload :connect
  [{:keys [client-id will-topic will-payload username password] :as packet} out]
  (encode-string out client-id)
  (if will-topic   (encode-string out will-topic))
  (if will-payload (encode-string out will-payload))
  (if username     (encode-string out username))
  (if password     (encode-string out password))
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
