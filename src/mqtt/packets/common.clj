(ns mqtt.packets.common
  (:import [io.netty.handler.codec EncoderException]))

;; packet encoding interface
(defmulti message-defaults :type)
(defmulti validate-message :type)
(defmulti remaining-length :type)
(defmulti encode-variable-header :type)
(defmulti encode-payload :type)

;; packet decoding interface
(defmulti decode-variable-header :type)
(defmulti decode-payload :type)

;; shared validations
(defn validate-message-id
  [qos message-id]
  (when (and qos
             (> qos 0)
             (or (nil? message-id)
                 (= 0 message-id)))
    (throw (new EncoderException))))
