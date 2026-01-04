package com.yuezm.project.connector

import com.google.protobuf.ByteString
import com.yuezm.project.connector.proto.ChunkMessage
import com.yuezm.project.connector.proto.DataSourceInfo
import groovyx.gpars.actor.Actors
import io.aeron.Aeron
import io.aeron.FragmentAssembler
import io.aeron.Publication
import io.aeron.Subscription
import io.aeron.driver.MediaDriver
import io.aeron.driver.ThreadingMode
import io.aeron.logbuffer.FragmentHandler
import org.agrona.concurrent.BackoffIdleStrategy
import org.agrona.concurrent.IdleStrategy
import org.agrona.concurrent.NoOpIdleStrategy
import org.agrona.concurrent.UnsafeBuffer
import com.yuezm.project.connector.proto.Response

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Application
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/10 17:06
 */
class Application {
    private static final int PORT = 38880
    private static final String SERVER_URL = "aeron:udp?endpoint=0.0.0.0:$PORT"
    private static final int STREAM_ID = 2500
    private static final int MAX_RETRIES = 10
    private static final int RETRY_DELAY_MS = 100
    private static final int MAX_PUBLICATIONS = 1000
    private static final int BUFFER_LENGTH = 2 * 1024 * 1024 // 2MB

    private final static AtomicBoolean running = new AtomicBoolean(true)
    private static Map<String, Publication> pubs = new ConcurrentHashMap<>()
    private static MediaDriver mediaDriver
    private static Aeron aeron
    private static Subscription sub
    private static DBServer server
    private static final int WORKER_COUNT =
            Math.max(2, Runtime.runtime.availableProcessors() / 2)
    private static List workers = []
    private static final AtomicInteger rr = new AtomicInteger(0)
    private static def responseSender

    private static void initWorkers() {
        WORKER_COUNT.times { idx ->
            workers << Actors.actor {
                loop {
                    react { WorkItem item ->
                        try {
                            //处理业务
                            handleBusiness(item.data)
                        } catch (Throwable t) {
                            println "Worker-$idx error: ${t.message}"
                            t.printStackTrace()
                        }
                    }
                }
            }
        }
        println "Initialized $WORKER_COUNT worker actors"
    }

    private static void initResponseSender() {
        responseSender = Actors.actor {
            loop {
                react { Response response ->
                    try {
                        sendResponseInternal(response)
                    } catch (Throwable t) {
                        println "Sender error: ${t.message}"
                        t.printStackTrace()
                    }
                }
            }
        }
    }

    private static void handleBusiness(byte[] data) {

        // 解析 DataSourceInfo 消息
        DataSourceInfo info = DataSourceInfo.parseFrom(data)

        // 处理消息
        Response resp = server.handle(data)
        resp = resp.toBuilder().setExec(info.exec).build()
        // 发送响应
        if (resp.hasExec() && resp.exec.hasRequestInfo()) {
            // 发送响应
            responseSender << resp
        }
    }

    static void main(String[] args) {
        try {
            println "=== Starting Aeron Server ==="
            println "OS: ${System.getProperty('os.name')} ${System.getProperty('os.arch')} ${System.getProperty('os.version')}"
            println "Java: ${System.getProperty('java.version')} ${System.getProperty('java.vendor')}"
            println "Max memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB"

            // 初始化服务器
            initialize()

            initWorkers()

            initResponseSender()

            // 添加关闭钩子
            addShutdownHook()



            // 启动消息处理循环
            runMessageLoop()


        } catch (Exception e) {
            println "Fatal error in main: ${e.message}"
            e.printStackTrace()
            System.exit(1)
        }
    }

    private static void initialize() {
        try {
            MediaDriver.Context driverContext = new MediaDriver.Context()
                    .aeronDirectoryName("aeron-data-${System.currentTimeMillis()}")
                    .threadingMode(ThreadingMode.DEDICATED)
                    .conductorIdleStrategy(new BackoffIdleStrategy(1, 1, 1000, 1000))
                    .receiverIdleStrategy(new NoOpIdleStrategy())
                    .senderIdleStrategy(new NoOpIdleStrategy())
                    .publicationTermBufferLength(8 * 1024 * 1024)  // 8MB
                    .initialWindowLength(5 * 1024 * 1024)  // 128KB, 匹配系统 SO_RCVBUF 限制
                    .socketRcvbufLength(5 * 1024 * 1024)   // 显式设置 socket 接收缓冲区大小
                    .socketSndbufLength(5 * 1024 * 1024)   // 显式设置 socket 发送缓冲区大小

            // 启动 MediaDriver
            mediaDriver = MediaDriver.launch(driverContext)

            // 配置 Aeron 客户端
            Aeron.Context aeronContext = new Aeron.Context()
                    .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                    .errorHandler { throwable ->
                        println "Aeron error: ${throwable.message}"
                        throwable.printStackTrace()
                    }
                    .availableImageHandler { image ->
                        println "New image: ${image.sourceIdentity()}, sessionId=${image.sessionId()}, position=${image.position()}"
                    }
                    .unavailableImageHandler { image ->
                        println "Lost image: ${image.sourceIdentity()}, sessionId=${image.sessionId()}"
                    }

            // 连接 Aeron
            aeron = Aeron.connect(aeronContext)

            // 创建订阅
            sub = aeron.addSubscription(SERVER_URL, STREAM_ID)

            // 初始化服务器
            server = new DBServer()

            println "Server initialized successfully. Listening on $SERVER_URL, streamId: $STREAM_ID"

        } catch (Exception e) {
            println "Failed to initialize server: ${e.message}"
            throw e
        }
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread({
            try {
                println "Shutdown hook triggered, closing resources..."
                running.set(false)

                // 关闭所有发布者
                pubs.each { _, pub ->
                    try {
                        if (!pub.isClosed()) {
                            pub.close()
                        }
                    } catch (Exception e) {
                        println "Error closing publication: ${e.message}"
                    }
                }
                pubs.clear()

                // 关闭订阅
                if (sub && !sub.isClosed()) {
                    sub.close()
                }

                // 关闭 Aeron 和 MediaDriver
                if (aeron) {
                    aeron.close()
                }

                if (mediaDriver) {
                    mediaDriver.close()
                }
                DBServer.shutdown()
                println "Server shutdown complete."
            } catch (Exception e) {
                println "Error during shutdown: ${e.message}"
                e.printStackTrace()
            }
        } as Runnable))
    }

    private static void runMessageLoop() {
        final IdleStrategy idle = new BackoffIdleStrategy(100, 10, 1000, 1000)
        int consecutiveErrors = 0
        final int MAX_CONSECUTIVE_ERRORS = 10

        println "Starting message processing loop..."

        try {
            final FragmentAssembler assembler =
                    new FragmentAssembler(handler)
            while (running.get()) {
                try {
                    // 处理消息


                    int fragmentsRead = sub.poll(assembler, 10)

                    // 如果没有消息处理，增加空闲计数
                    if (fragmentsRead == 0) {
                        idle.idle()
                        consecutiveErrors = 0  // 重置连续错误计数
                    } else {
                        idle.reset()
                    }

                    // 定期清理关闭的发布者
                    if (System.currentTimeMillis() % 60000 == 0) {  // 每分钟清理一次
                        cleanClosedPublications()
                    }

                } catch (Exception e) {
                    consecutiveErrors++
                    println "Error in message loop (${consecutiveErrors}/$MAX_CONSECUTIVE_ERRORS): ${e.message}"

                    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                        println "Too many consecutive errors (${consecutiveErrors}), shutting down..."
                        running.set(false)
                        break
                    }

                    try {
                        Thread.sleep(1000)  // 错误后短暂休眠
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt()
                        break
                    }
                }
            }
        } finally {
            println "Message loop stopped"
        }
    }

    private static void cleanClosedPublications() {
        int initialSize = pubs.size()
        pubs.entrySet().removeIf { entry ->
            try {
                if (entry.value.isClosed()) {
                    println "Removing closed publication: ${entry.key}"
                    return true
                }
                return false
            } catch (Exception e) {
                println "Error checking publication status: ${e.message}"
                return true
            }
        }

        int removed = initialSize - pubs.size()
        if (removed > 0) {
            println "Cleaned up $removed closed publications. Current active: ${pubs.size()}"
        }
    }

    private static final FragmentHandler handler = { buffer, offset, length, header ->
        try {
            if (length <= 0) {
                println "Received empty message"
                return
            }
            println "Received message, length: $length, session: ${header.sessionId()}, position: ${header.position()}"
            byte[] data = new byte[length]
            buffer.getBytes(offset, data)

            int idx = Math.abs(rr.getAndIncrement() % workers.size())
            workers[idx] << new WorkItem(data)


        } catch (Exception e) {
            println "Error in message handler: ${e.message}"
            e.printStackTrace()
        }
    }

    private static Publication getOrCreatePublication(Aeron aeron, String key, String channel, int streamId) {
        try {
            return pubs.computeIfAbsent(key, k -> {
                if (pubs.size() >= MAX_PUBLICATIONS) {
                    println "Maximum number of publications ($MAX_PUBLICATIONS) reached, cannot create new one for $k"
                    return null
                }

                println "Creating new publication for $k"
                try {
                    def pub = aeron.addExclusivePublication(channel, streamId)
                    // Wait for the publication to be connected
                    int retry = 0
                    while (!pub.isConnected() && retry++ < 10) {
                        println "Waiting for publication to connect... (attempt $retry/10)"
                        Thread.sleep(100)
                    }
                    if (!pub.isConnected()) {
                        println "Failed to connect publication after 10 attempts"
                        return null
                    }
                    println "Publication connected: $pub"
                    return pub
                } catch (Exception e) {
                    println "Failed to create publication for $k: ${e.message}"
                    return null
                }
            })
        } catch (Exception e) {
            println "Error getting/creating publication for $key: ${e.message}"
            return null
        }
    }

    private static void sendResponseInternal(Response resp) {
        if (!resp.hasExec() || !resp.exec.hasRequestInfo()) return

        def ri = resp.exec.requestInfo
        String key = "${ri.replyChannel}:${ri.replyStream}"

        Publication pub = pubs.computeIfAbsent(key) {
            aeron.addExclusivePublication(ri.replyChannel, ri.replyStream)
        }

        byte[] out = resp.toByteArray()
        int totalLen = out.length
        int maxChunkSize = 1024 * 1024 - 1024
        String messageId = UUID.randomUUID().toString()

        int totalChunks = (totalLen + maxChunkSize - 1) / maxChunkSize as int

        IdleStrategy idle = new BackoffIdleStrategy(1, 10, 1000, 10000)

        for (int i = 0; i < totalChunks; i++) {
            int start = i * maxChunkSize
            int len = Math.min(maxChunkSize, totalLen - start)
            println "total len : $totalLen, current index: $i, start: $start end: $len"
            ChunkMessage msg = ChunkMessage.newBuilder()
                    .setMessageId(messageId)
                    .setTotalChunks(totalChunks)
                    .setChunkIndex(i)
                    .setPayload(ByteString.copyFrom(out, start, len))
                    .build()

            UnsafeBuffer buf = new UnsafeBuffer(msg.toByteArray())

            while (true) {
                long r = pub.offer(buf)
                if (r > 0) {
                    idle.reset()
                    break
                }

                if (r == Publication.BACK_PRESSURED ||
                        r == Publication.ADMIN_ACTION ||
                        r == Publication.NOT_CONNECTED) {
                    idle.idle()
                    continue
                }

                if (r == Publication.CLOSED) {
                    pubs.remove(key)
                    return
                }

                throw new IllegalStateException("Unexpected offer result: " + r)
            }
        }
    }


}
