(ns mqtt.encoding-utils)

(defn utf8-bytes
  [s]
  (.getBytes (str s) "UTF-8"))

(defn encode-byte
  "Encode a single byte"
  [out b]
  (.writeByte out b))

(defn encode-bytes
  "Encode a bunch of bytes"
  [out bs]
  (.writeBytes out bs))

(defn encode-unsigned-short
  "Encode an unsigned short"
  [out i]
  (.writeShort out i))

(defn encode-string
  "Encode a utf-8 encoded string. Strings are preceeded by 2 bytes describing
  the length of the remaining content."
  [out string]
  (let [bs (utf8-bytes string)]
    (encode-unsigned-short out (count bs))
    (encode-bytes out bs)))
