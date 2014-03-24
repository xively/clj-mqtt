(ns mqtt.codec
  (:use mqtt.encoder
        mqtt.decoder)
  (:import [io.netty.channel CombinedChannelDuplexHandler]))

(defn make-codec
  [& {:keys [error-fn]
      :or {error-fn identity}}]
  (CombinedChannelDuplexHandler. (make-decoder :error-fn error-fn)
                                 (make-encoder :error-fn error-fn)))
