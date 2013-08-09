(ns mqtt.packets.pingreq
  (:use mqtt.decoding-utils
        mqtt.packets.common))

;; No variable header, nothing to do.
(defmethod decode-variable-header :pingreq
  [packet in]
  packet)

;; No payload, nothing to do.
(defmethod decode-payload :pingreq
  [packet in]
  packet)

