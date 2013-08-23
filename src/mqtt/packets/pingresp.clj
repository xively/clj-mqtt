(ns mqtt.packets.pingresp
  (:use mqtt.packets.common))

;; No variable header, nothing to do.
(defmethod decode-variable-header :pingresp
  [packet in]
  packet)

;; No payload, nothing to do.
(defmethod decode-payload :pingresp
  [packet in]
  packet)

(defmethod message-defaults :pingresp
  [message]
  {})

(defmethod validate-message :pingresp
  [message]
  message)

(defmethod remaining-length :pingresp
  [packet]
  0)

(defmethod encode-variable-header :pingresp
  [packet out]
  packet)

(defmethod encode-payload :pingresp
  [packet out]
  packet)
