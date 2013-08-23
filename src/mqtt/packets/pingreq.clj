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

(defmethod message-defaults :pingreq
  [packet]
  {})

(defmethod validate-message :pingreq
  [packet]
  packet)

(defmethod remaining-length :pingreq
  [packet]
  0)

(defmethod encode-variable-header :pingreq
  [packet out]
  packet)

(defmethod encode-payload :pingreq
  [packet out]
  packet)
