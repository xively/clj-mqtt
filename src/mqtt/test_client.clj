(ns mqtt.test-client
  (:require [clojure.core.async :as async]
            [mqtt.codec :as codec])
  (:import [com.xively.netty Netty]
           [io.netty.bootstrap Bootstrap]
           [io.netty.channel Channel SimpleChannelInboundHandler ChannelInitializer ChannelHandler ChannelHandlerContext ChannelFuture ChannelPipeline]
           [io.netty.channel.nio NioEventLoopGroup]
           [io.netty.channel.socket.nio NioSocketChannel]
           [java.net InetSocketAddress]))

(def ^:dynamic *default-host* "localhost")
(def ^:dynamic *default-port* 1883)

(defn- consume
  "Consume messages from a given channel calling f on evey message"
  [chan f]
  (async/go-loop []
    (try
      (let [msg (async/<! chan)]
        (when-not (nil? msg)
          (f msg)
          (recur)))
      (catch Exception e e))))

(defn- start-sender [sock channel]
  (consume (:out sock) #(let [[done msg] %1]
                          (Netty/write channel msg)
                          (Netty/flush channel)
                          (async/close! done))))

(declare close)
(defn- gen-response-handler [{:keys [subscriptions in unsubscribed-messages] :as sock}]
  (proxy [SimpleChannelInboundHandler] []
    (channelRead0 [ctx msg]
      (let [chan (if (= :publish (:type msg))
                   (or (-> @subscriptions (get (:topic msg)) :chan)
                       unsubscribed-messages)
                   in)]
        (async/put! chan msg)))

    (channelInactive [ctx]
      (close (assoc sock :future ctx)))

    (exceptionCaught
      [^ChannelHandlerContext ctx cause]
      (close sock)
      (throw cause))))

(defn- gen-channel-initializer
  [sock ^ChannelHandler handler]
  (proxy [ChannelInitializer] []
    (initChannel [^Channel ch]
      (reset! (:channel sock) ch)
      (start-sender sock ch)
      (doto (Netty/pipeline ch)
        (.addLast "codec" ^ChannelHandler (codec/make-codec))
        (.addLast "handler" handler)))))

(defn socket
  "Creates and connects a raw mqtt socket to the address. Returns two chans an input one and an output one."
  ([] (socket (str "tcp://" *default-host* ":" *default-port*)))
  ([addr]
     (let [uri (java.net.URI. addr)
           subscriptions (atom {})
           sock {:in (async/chan)
                 :out (async/chan)
                 :unsubscribed-messages (async/chan)
                 :next-id (atom 0)
                 :subscriptions subscriptions
                 :client-id (atom nil)
                 :channel (atom nil)}
           bootstrap (Bootstrap.)]
       (Netty/group bootstrap (NioEventLoopGroup.))
       (Netty/clientChannel bootstrap NioSocketChannel)
       (Netty/handler bootstrap (gen-channel-initializer sock (gen-response-handler sock)))

       (assoc sock :future (-> (.connect bootstrap (.getHost uri) (.getPort uri))
                               (.await))))))

(defn get-next-message-id [socket]
  ;; TODO modulus Short/MAX_VALUE
  (swap! (:next-id socket) inc))

(defn unsubscribed-messages [socket]
  (:unsubscribed-messages socket))

(defn recv-message
  [socket]
  (async/<!! (:in socket)))

(defn- pipeline
  ^ChannelPipeline [{:keys [channel] :as socket}]
  (.pipeline ^Channel @channel))

(defn- first-context
  ^ChannelHandlerContext [socket]
  (.firstContext ^ChannelPipeline (pipeline socket)))

(defn- context
  ^ChannelHandlerContext [socket ^String handler-name]
  (let [pipeline (pipeline socket)]
    (.context ^ChannelPipeline pipeline (.get pipeline handler-name))))

(defn raw-send-message
  [socket msg]
  (-> socket
      first-context
      (Netty/writeAndFlush msg)))

(defn send-message
  "Send a packet from the client. We use the 'done' channel as a mutex so we
  can block until the send is done."
  [socket msg]
  (let [done (async/chan)]
    (async/>!! (:out socket) [done msg])
    (async/<!! done)))

(defn expect-message
  [socket f & {:keys [timeout]}]
  (let [msg (first (async/alts!! [(:in socket) (async/timeout (or timeout 1000))]))]
    (cond
     (nil? msg) (throw (Exception. "No message received"))
     (not (f msg)) (throw (Exception. (str "Unexpected message received " msg)))
     :else msg)))

(defn connect
  [socket opts]
  (let [packet (merge {:type :connect
                       :client-id (str (gensym "test_client"))}
                      opts)]
    (send-message socket packet)
    (expect-message socket #(= :accepted (:return-code %)) :timeout 2000)
    (reset! (:client-id socket) (:client-id packet))))

(defn publish
  [socket topic payload & {:keys [qos message-id wait]
                           :or {qos 1
                                message-id (get-next-message-id socket)
                                wait true}}]
  (send-message socket {:type :publish
                        :topic topic
                        :payload payload
                        :qos qos
                        :message-id message-id})
  (when wait
    (condp = qos
      0
      nil

      1
      (expect-message socket #(and (= :puback (:type %))
                                   (= message-id (:message-id %))))

      2
      (do
        (expect-message socket #(and (= :pubrec (:type %))
                                     (= message-id (:message-id %))))
        (send-message socket {:type :pubrel
                              :message-id message-id})
        (expect-message socket #(and (= :pubcomp (:type %))
                                     (= message-id (:message-id %)))))))
  true)

(defn subscribe
  [socket topic-qos-pairs]
  (let [message-id (get-next-message-id socket)
        received (async/chan)
        new-subscriptions (->> topic-qos-pairs
                               (map (fn [[topic qos]] [topic {:chan received, :qos qos}]))
                               (into {}))]
    (send-message socket {:type :subscribe
                          :qos 1
                          :message-id message-id
                          :topics topic-qos-pairs})
    (expect-message socket #(and (= :suback (:type %))
                                 (= message-id (:message-id %))))
    (swap! (:subscriptions socket) merge new-subscriptions)
    received))

(defn ping
  [socket]
  (send-message socket {:type :pingreq})
  (expect-message socket #(= :pingresp (:type %))))

(defn open?
  "Check if a socket is currently connected."
  [socket]
  (try
    (.isActive (.channel (:future socket)))
    (catch Exception e false)))

(def closed? (complement open?))

(defn connected?
  "Check if a socket is currently connected."
  [socket]
  (when (open? socket)
    (try
      (ping socket)
      true
      (catch Exception e
        (when-not (= "No message received" (.getMessage e))
          (throw e))
        false))))

(defn disconnected?
  "Check if a socket receives no more messages and is disconnected."
  [socket & {:keys [timeout] :or {timeout 1000}}]
  (let [timeout (async/timeout timeout)
        [msg chan] (async/alts!! [(:in socket) timeout])]
    (cond
      (= chan timeout)
      (throw (Exception. "Was not disconnected within timeout"))

      (nil? msg)
      (not (connected? socket))

      :else
      (throw (Exception. (str "Received unexpected message" msg))))))

(defn close
  [socket]
  (.await (Netty/close (.channel (:future socket))))
  (async/close! (:out socket))
  (async/close! (:in socket))
  (async/close! (:unsubscribed-messages socket))
  (doseq [{:keys [chan]} (vals @(:subscriptions socket))]
    (async/close! chan)))

(defn disconnect
  [socket]
  (send-message socket {:type :disconnect})
  (close socket))

(defn connack-reason
  [socket reason ]
  (expect-message socket #(and (= :connack (:type %))
                               (= reason (:return-code %))) :timeout 2000))

(defmacro with-client
  [[client opts] & body]
  `(let [defaults# {:protocol "tcp"
                    :host *default-host*
                    :port *default-port*}
         opts# (merge defaults# ~opts)
         addr# (format "%s://%s:%s" (:protocol opts#) (:host opts#) (:port opts#))
         opts# (dissoc opts# :protocol :host :port)
         ~client (socket addr#)]
     (try
       (connect ~client opts#)
       ~@body
       (finally
         (close ~client)))))
