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

