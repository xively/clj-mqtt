(ns mqtt.codec
  (:use mqtt.encoder
        mqtt.decoder)
  (:import [io.netty.channel CombinedChannelDuplexHandler]))

(defn make-codec
  []
  (CombinedChannelDuplexHandler. (make-decoder) (make-encoder)))
