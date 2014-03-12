(ns mqtt.decoding-utils
  (:import [java.nio.charset Charset]
           [java.io StreamCorruptedException]
           [io.netty.buffer ByteBuf]))

(defn assert-readable-bytes
  "Make sure that there are at least 'expected' more bytes to be read from the
  buffer. This is to protect against corrupt packets."
  [^ByteBuf buffer expected]
  (let [remaining (.readableBytes buffer)]
    (when (< remaining expected)
      (throw (new StreamCorruptedException (str "Expected " expected " bytes, but only " remaining " remaining."))))))

(defn parse-unsigned-byte
  "Decode a single byte"
  [^ByteBuf in]
  (assert-readable-bytes in 1)
  (.readUnsignedByte in))

(defn parse-unsigned-short
  "Decode an unsigned short"
  ^Integer [^ByteBuf in]
  (assert-readable-bytes in 2)
  (.readUnsignedShort in))

(defn- parse-flag
  [flags pos width]
  (let [max-val (int (- (Math/pow 2 width) 1))
        val (bit-and max-val (bit-shift-right flags pos))]
    (if (= 1 width)
      (= 1 val)
      val)))

(defn- do-parse-flags
  [flags m pos [key width & kvs]]
  (let [newpos (- pos width)
        ret (assoc m key (parse-flag flags newpos width))]
    (if kvs
      (recur flags ret newpos kvs)
      ret)))

(defn parse-flags
  "Decode a single byte of flags. Takes a list of pairs of keywords and
  bit-widths. All bit-widths must add up to 8. All 1-bit values are converted
  to boolean.

  Example:

    (parse-flags buffer :type 4, :duplicate 1, :qos 2, :retain 1)

  "
  [in & kvs]
  (do-parse-flags (parse-unsigned-byte in) {} 8 kvs))

(defn parse-short-prefixed-bytes
  "Decode a short, then as many bytes as the short says."
  [^ByteBuf in]
  (let [len (int (parse-unsigned-short in))]
    (assert-readable-bytes in len)
    (let [bs (byte-array len)]
      (.readBytes in bs)
      bs)))

(def ^:private utf-8 (Charset/forName "UTF-8"))

(defn unsigned-byte
  "Cast to unsigned byte.
  256 is Byte/MAX_VALUE - Byte/MIN_VALUE."
  [b]
  (cond
    (> b Byte/MAX_VALUE) (recur (- b 256))
    (< b Byte/MIN_VALUE) (recur (+ b 256))
    :else (byte b)))

(defn has-bad-bytes?
  "Don't allow \u0000, \ud800 to \udfff"
  [in]
  (let [[ch0 ch1 ch2 & rst] (seq in)]
    (when ch0
      (or (= ch0 (unsigned-byte 0x00))
          (and (= ch0 (unsigned-byte 0xed))
               (>= ch1 (unsigned-byte 0xa0))
               (>= ch2 (unsigned-byte 0x80))
               (<= ch1 (unsigned-byte 0xbf))
               (<= ch2 (unsigned-byte 0xbf)))
          (recur rst)))))

(defn parse-string
  "Decode a utf-8 encoded string. Strings are preceeded by 2 bytes describing
  the length of the remaining content."
  [^ByteBuf in]
  (let [len (int (parse-unsigned-short in))
        _   (assert-readable-bytes in len)
        bs  (byte-array len)]
    (.readBytes in bs)
    (when (has-bad-bytes? bs) (throw (Exception. "invalid UTF-8")))
    (String. bs utf-8)))
