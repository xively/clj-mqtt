(ns mqtt.packets.pingresp
  (:use mqtt.packets.common))

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
