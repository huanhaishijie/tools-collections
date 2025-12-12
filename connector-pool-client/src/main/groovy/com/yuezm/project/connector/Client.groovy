import com.yuezm.project.connector.Chat
import com.yuezm.project.connector.proto.DataSourceInfo
import com.yuezm.project.connector.proto.ExecInfo
import com.yuezm.project.connector.proto.RequestInfo
import com.yuezm.project.connector.proto.Response
import groovyx.gpars.actor.Actors
import io.aeron.Aeron
import io.aeron.Publication
import io.aeron.Subscription
import io.aeron.driver.MediaDriver
import io.aeron.logbuffer.FragmentHandler
import org.agrona.concurrent.BackoffIdleStrategy
import org.agrona.concurrent.IdleStrategy
import org.agrona.concurrent.UnsafeBuffer

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class Client {
    String host = "127.0.0.1"
    String serverHost = "127.0.0.1"
    private static final int serverStreamId = 2500
    int port
    int streamId
    private static final String CLIENT_URL_PREFIX = "aeron:udp?endpoint="
    private static Client instance = null
    private static Aeron aeron
    private static MediaDriver mediaDriver
    private static final AtomicBoolean running = new AtomicBoolean(true)
    private static Publication serverPub
    private static final ConcurrentHashMap<String, Chat> chats = [:]
    private static Subscription clientSub

    private Client(String host = "127.0.0.1", int port = 38881, int streamId = 2500, String serverHost = "127.0.0.1") {
        this.host = host
        this.port = port
        this.streamId = streamId
        this.serverHost = serverHost
    }

    static Client getInstance(String host = "127.0.0.1", int port = 38881, int streamId = 2500, String serverHost = "127.0.0.1") {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client(host, port, streamId, serverHost)
                    initAeron()
                }
            }
        }
        return instance
    }

    private static void initAeron() {
        // 初始化 MediaDriver
        mediaDriver = MediaDriver.launchEmbedded()

        // 初始化 Aeron 客户端
        aeron = Aeron.connect(new Aeron.Context()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                .availableImageHandler { image -> println "Available image: ${image.sourceIdentity()}" }
                .unavailableImageHandler { image -> println "Unavailable image: ${image.sourceIdentity()}" })

        // 添加关闭钩子
        addShutdownHook()

        // 启动接收消息的actor
        startMessageReceiver()
    }

    private static void startMessageReceiver() {
        // 创建订阅
        clientSub = aeron.addSubscription(CLIENT_URL_PREFIX + instance.host + ":" + instance.port, instance.streamId)

        // 启动actor处理接收到的消息
        Actors.actor {
            final IdleStrategy idle = new BackoffIdleStrategy()
            final FragmentHandler handler = { buffer, offset, length, header ->
                try {
                    byte[] data = new byte[length]
                    buffer.getBytes(offset, data)
                    Response res = Response.parseFrom(data)
                    String chatId = res.getExec().getRequestInfo().getChatId()
                    if (chats[chatId]) {
                        chats[chatId].receiveMessage = res.getDataOrDefault("res", null)
                    }
                } catch (Exception e) {
                    println "Error processing message: ${e.message}"
                    e.printStackTrace()
                }
            }

            while (running.get()) {
                int fragments = clientSub.poll(handler, 10)
                idle.idle(fragments)
            }
        }

        // 创建发布
        serverPub = aeron.addPublication("aeron:udp?endpoint=${instance.serverHost}:38880", serverStreamId)
    }

    private static void addShutdownHook() {
        Runtime.runtime.addShutdownHook(new Thread({
            println "Shutting down client..."
            running.set(false)
            try { clientSub?.close() } catch (e) { e.printStackTrace() }
            try { serverPub?.close() } catch (e) { e.printStackTrace() }
            try { aeron?.close() } catch (e) { e.printStackTrace() }
            try { mediaDriver?.close() } catch (e) { e.printStackTrace() }
            println "Client shutdown complete"
        } as Runnable))
    }

    void send(DataSourceInfo dataSourceInfo, Closure callback) {
        // 构建 DataSourceInfo
        def chat = new Chat()
        chat.addPropertyChangeListener callback
        chats[chat.chatId] = chat
        dataSourceInfo.getExec().getRequestInfo().setChatId(chat.chatId)
        try {
            byte[] out = dataSourceInfo.toByteArray()
            UnsafeBuffer outBuf = new UnsafeBuffer(out)
            long result = serverPub.offer(outBuf, 0, out.length)
            if (result < 0) {
                println "Failed to send message, result: $result"
            } else {
                println "Message sent successfully"
            }
        } catch (Exception e) {
            println "Error sending message: ${e.message}"
            e.printStackTrace()
        }
    }

    static void main(String[] args) {
        def client = null
        try {
            client = Client.getInstance("127.0.0.1", 38881, 2500, "127.0.0.1")

            def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                    ExecInfo.newBuilder().setRequestInfo(
                            RequestInfo.newBuilder()
                                    .setReplyChannel(CLIENT_URL_PREFIX + "127.0.0.1:38881")
                                    .setReplyStream(2500)
                                    .build()
                    ).setMethod("registerDb").build()
            ).setUrl("jdbc:mysql://192.168.111.244:63306/shujuzhiliang")
                    .setType("com.mysql.cj.jdbc.Driver")
                    .setUsername("root")
                    .setPassword("skzz@2021")
                    .setMaxPoolSize(10)
                    .setMinPoolSize(1)
                    .setIdleTimeout(60000)
                    .setConnectionTimeout(30000)
                    .build()

            // 发送消息
            client.send dataSourceInfo, { backInfo ->
                println "Received response: ${backInfo}"
            }

            // 保持程序运行，等待用户输入后退出
            println "程序运行中，按 Enter 键退出..."
            System.in.read()
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            // 显式关闭资源
            if (client != null) {
                println "正在关闭资源..."
                try { clientSub?.close() } catch (e) { e.printStackTrace() }
                try { serverPub?.close() } catch (e) { e.printStackTrace() }
                try { aeron?.close() } catch (e) { e.printStackTrace() }
                try { mediaDriver?.close() } catch (e) { e.printStackTrace() }
                running.set(false)
                println "资源已关闭"
            }
        }
    }
}