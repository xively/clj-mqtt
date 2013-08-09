(ns mqtt.core)

(defn- flip-map
  "Invert a map, turn keys to values and values to keys"
  [m]
  (zipmap (vals m) (keys m)))

(def message-types { ;; 0 :reserved ;; commented so it returns nil
                     1 :connect
                     2 :connack
                     3 :publish
                     4 :puback
                     5 :pubrec
                     6 :pubrel
                     7 :pubcomp
                     8 :subscribe
                     9 :suback
                     10 :unsubscribe
                     11 :unsuback
                     12 :pingreq
                     13 :pingresp
                     14 :disconnect
                     ;;15 :reserved
                   })

(def message-type-byte (flip-map message-types))

(def connack-return-codes { 0 :accepted
                            1 :unacceptable-protocol-version
                            2 :identifier-rejected
                            3 :server-unavailable
                            4 :bad-username-or-password
                            5 :unauthorized })

(def connack-return-code-byte (flip-map connack-return-codes))
