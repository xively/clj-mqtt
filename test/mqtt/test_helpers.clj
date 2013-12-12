(ns mqtt.test-helpers
  (:import [java.nio ByteBuffer]
           [io.netty.buffer Unpooled]
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
                      (map unsigned-byte (.getBytes a "UTF-8"))
                      (unsigned-byte a)))]
    (flatten (map convert bs))))

(defn bytes-to-byte-array
  "Take a bunch of bytes or strings and make an array of bytes"
  [ & bs ]
  (byte-array (flatten-bytes bs)))

(defn bytes-to-byte-buffer
  "Take a bunch of bytes or strings and make a io.netty.buffer.ByteBuf"
  [ & bs ]
  (Unpooled/wrappedBuffer (apply bytes-to-byte-array bs)))

(defn bytes-to-java-byte-buffer
  "Take a bunch of bytes or strings and make a java.nio.ByteBuffer"
  [ & bs ]
  (ByteBuffer/wrap (apply bytes-to-byte-array bs)))

(defn byte-buffer-to-bytes
  "Take a byte buffer and read all it's bytes into an array for printing. This
  will modify the buffer."
  [buffer]
  (into [] (for [i (range (.readableBytes buffer))] (.readByte buffer))))
