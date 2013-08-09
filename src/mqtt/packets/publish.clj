(ns mqtt.packets.publish
  (:use mqtt.decoding-utils
        mqtt.encoding-utils
        mqtt.packets.common)
  (:require [clojure.string :as string])
  (:import [java.nio.charset Charset]
           [io.netty.handler.codec EncoderException]))

(defn- has-message-id
  [{:keys [qos]}]
  (and qos (pos? qos)))

(defn- parse-message-id
  [packet in]
  (when (has-message-id packet)
    (parse-unsigned-short in)))

(defmethod decode-variable-header :publish
  [packet in]
  (assoc packet :topic (parse-string in)
                :message-id (parse-message-id packet in)))

(defmethod decode-payload :publish
  [packet in]
  (assoc packet :payload (.toString (.readBytes in (.readableBytes in)) (Charset/forName "UTF-8"))))

(defmethod message-defaults :publish
  [message]
  message)

(defmethod validate-message :publish
  [{:keys [topic payload qos message-id]}]
  (if (string/blank? topic)                 (throw (new EncoderException)))
  (if (nil? payload)                        (throw (new EncoderException)))
  (validate-message-id qos message-id))

(defmethod remaining-length :publish
  [{:keys [qos topic payload] :as packet}]
  (+ 2 (count (utf8-bytes topic))       ;; topic length
     (if (has-message-id packet) 2 0)  ;; if we need to include a message id
     (count (utf8-bytes payload))))     ;; payload length

(defmethod encode-variable-header :publish
  [{:keys [topic message-id] :as packet} out]
  (encode-string out topic)
  (if (has-message-id packet)
    (encode-unsigned-short out message-id))
  packet)

(defmethod encode-payload :publish
  [{:keys [payload] :as packet} out]
  (encode-bytes out (utf8-bytes payload))
  packet)
