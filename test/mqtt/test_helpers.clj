(ns mqtt.test-helpers
  (:import [java.nio ByteBuffer]
           [io.netty.buffer Unpooled ByteBuf]
           [io.netty.channel DefaultChannelHandlerContext]))

(defn unsigned-byte
  "Cast to unsigned byte.
  256 is Byte/MAX_VALUE - Byte/MIN_VALUE."
  [b]
  (cond
    (> b Byte/MAX_VALUE) (recur (- b 256))
    (< b Byte/MIN_VALUE) (recur (+ b 256))
    :else (byte b)))

(defn- flatten-bytes
  [bs]
  (let [convert   (fn [a]
                    (if (isa? (class a) String)
                      (map unsigned-byte (.getBytes ^String a "UTF-8"))
                      (unsigned-byte a)))]
    (flatten (map convert bs))))

(defn bytes-to-byte-array
  "Take a bunch of bytes or strings and make an array of bytes"
  ^bytes [ & bs ]
  (byte-array (flatten-bytes bs)))

(defn bytes-to-byte-buffer
  "Take a bunch of bytes or strings and make a io.netty.buffer.ByteBuf"
  [ & bs ]
  (Unpooled/wrappedBuffer ^bytes (apply bytes-to-byte-array bs)))

(defn bytes-to-java-byte-buffer
  "Take a bunch of bytes or strings and make a java.nio.ByteBuffer"
  [ & bs ]
  (ByteBuffer/wrap (apply bytes-to-byte-array bs)))

(defn byte-buffer-to-byte-array
  "Take a byte buffer and read all it's bytes into an array for printing. This
  will modify the buffer."
  ^bytes [^ByteBuf buffer]
  (let [bs (byte-array (.readableBytes buffer))]
    (.readBytes buffer bs)
    bs))

(defn byte-buffer-to-bytes
  "Take a byte buffer and read all it's bytes into a clojure array for printing. This
  will modify the buffer."
  [^ByteBuf buffer]
  (into [] (byte-buffer-to-byte-array buffer)))

(defn byte-buffer-to-string
  "Take a byte buffer and read all it's bytes into a string for printing. This
  will modify the buffer."
  ^String [^ByteBuf buffer]
  (String. (byte-buffer-to-byte-array buffer) "UTF-8"))
