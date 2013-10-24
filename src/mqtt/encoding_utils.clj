(ns mqtt.encoding-utils
  (:import [io.netty.buffer ByteBuf]))

(defn utf8-bytes
  [s]
  (.getBytes (str s) "UTF-8"))

(defn encode-byte
  "Encode a single byte"
  [^ByteBuf out b]
  (.writeByte out b)
  out)

(defn encode-bytes
  "Encode a bunch of bytes"
  [^ByteBuf out #^bytes bs]
  (.writeBytes out bs)
  out)

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
    (encode-bytes out bs)))
