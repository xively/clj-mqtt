(ns mqtt.encoding-utils
  (:import [java.nio ByteBuffer]
           [io.netty.buffer ByteBuf]))

(defn utf8-bytes
  ^bytes [s]
  (.getBytes (str s) "UTF-8"))

(defn encode-byte
  "Encode a single byte"
  [^ByteBuf out b]
  (.writeByte out b)
  out)


(defprotocol EncodeBytes
  (encode-bytes [bs out] "Encode a bunch of bytes")
  (remaining-bytes [bs] "How many bytes are remaining to encode?"))

(extend-protocol EncodeBytes
  String
  (encode-bytes [s ^ByteBuf out]
    (.writeBytes out (utf8-bytes s)))
  (remaining-bytes [s]
    (count (utf8-bytes s)))

  ByteBuf
  (encode-bytes [buf ^ByteBuf out]
    (.writeBytes out ^ByteBuf buf))
  (remaining-bytes [buf]
    (.readableBytes buf))

  ByteBuffer
  (encode-bytes [buf ^ByteBuf out]
    (.writeBytes out ^ByteBuffer buf))
  (remaining-bytes [buf]
    (.remaining buf))

  #=(java.lang.Class/forName "[B")
  (encode-bytes [bs ^ByteBuf out]
    (.writeBytes out ^bytes bs))
  (remaining-bytes [bs]
    (count bs)))

(defn encode-unsigned-short
  "Encode an unsigned short"
  [^ByteBuf out i]
  (.writeShort out i)
  out)

(defn encode-string
  "Encode a utf-8 encoded string. Strings are preceeded by 2 bytes describing
  the length of the remaining content."
  [^ByteBuf out string]
  (let [bs (utf8-bytes string)]
    (encode-unsigned-short out (count bs))
    (encode-bytes bs out)))
