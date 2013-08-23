(ns mqtt.packets.disconnect
  (:use mqtt.decoding-utils
        mqtt.packets.common))

;; No variable header, nothing to do.
(defmethod decode-variable-header :disconnect
  [packet in]
  packet)

;; No payload, nothing to do.
(defmethod decode-payload :disconnect
  [packet in]
  packet)

(defmethod message-defaults :disconnect
  [packet]
  {})

(defmethod validate-message :disconnect
  [packet]
  packet)

(defmethod remaining-length :disconnect
  [packet]
  0)

(defmethod encode-variable-header :disconnect
  [packet out]
  packet)

(defmethod encode-payload :disconnect
  [packet out]
  packet)
