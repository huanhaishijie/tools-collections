package com.zz.datacenter

import com.yuezm.project.connector.proto.*
import groovyx.gpars.actor.Actors
import io.aeron.Aeron
import io.aeron.FragmentAssembler
import io.aeron.Publication
import io.aeron.Subscription
import io.aeron.driver.MediaDriver
import io.aeron.logbuffer.FragmentHandler
import org.agrona.concurrent.BackoffIdleStrategy
import org.agrona.concurrent.IdleStrategy
import org.agrona.concurrent.UnsafeBuffer

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Client {
    String host = "127.0.0.1"
    String serverHost = "127.0.0.1"
    private static final int serverStreamId = 2500
    int port
    int streamId
    String model
    String shell

    private static final String CLIENT_URL_PREFIX = "aeron:udp?endpoint="
    static Client instance = null
    private static Aeron aeron
    private static MediaDriver mediaDriver
    private static final AtomicBoolean running = new AtomicBoolean(true)
    private static Publication serverPub
    private static final ConcurrentHashMap<String, Chat> chats = [:]
    private static Subscription clientSub

    private static final AtomicInteger rr = new AtomicInteger(0)
    private static List workers = []

    private static volatile Map<String, List<byte[]>> messageBuffer = [:]

    private Client(String host = "127.0.0.1", int port = 38881, int streamId = 2500, String serverHost = "127.0.0.1", String model = "local", String shell = "") {
        this.host = host
        this.port = port
        this.streamId = streamId
        this.serverHost = serverHost
        this.model = model
        this.shell = shell
    }

    static Client getInstance(String host = "127.0.0.1", int port = 38881, int streamId = 2500, String serverHost = "127.0.0.1", String model = "local", String shell = "") {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client(host, port, streamId, serverHost, model, shell)
                    initAeron()
                }
            }
        }
        return instance
    }

    private static void initAeron() {
        // 启动 MediaDriver 并清理旧目录
        mediaDriver = MediaDriver.launch(
                new MediaDriver.Context()
                        .dirDeleteOnStart(true)
                        .dirDeleteOnShutdown(true)
        )

        // 初始化 Aeron 客户端
        aeron = Aeron.connect(
                new Aeron.Context()
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
        )

        addShutdownHook()
        initWorker()
        startMessageReceiver()
    }

    private static void initWorker() {
        2.times {
            workers << Actors.actor {
                loop {
                    react { WorkItem workItem ->
                        try {
                            ChunkMessage chunk = ChunkMessage.parseFrom(workItem.data)
                            String messageId = chunk.getMessageId()
                            // 初始化分片列表
                            if (!messageBuffer.containsKey(messageId)) {
                                messageBuffer[messageId] = new ArrayList<>(chunk.getTotalChunks())
                                for (int i = 0; i < chunk.getTotalChunks(); i++) messageBuffer[messageId].add(null)
                            }
                            // 存放分片
                            messageBuffer[messageId][chunk.getChunkIndex()] = chunk.getPayload().toByteArray()
                            // 检查是否收齐
                            if (!messageBuffer[messageId].contains(null)) {
                                // 拼接完整数据
                                ByteArrayOutputStream baos = new ByteArrayOutputStream()
                                messageBuffer[messageId].each { baos.write(it) }
                                byte[] fullData = baos.toByteArray()

                                // 解析完整 Response
                                Response res = Response.parseFrom(fullData)
                                String chatId = res.getExec().getRequestInfo().getChatId()
                                if (res.getCode() != 0) {
                                    throw new RuntimeException("chatId: ${chatId}, code:${res.getCode()} error: ${res.getMessage()}")
                                }

                                if (chats[chatId]) {
                                    def r = res.getDataOrDefault("res", null)
                                    if(r == "rowset"){
                                        chats[chatId].receiveMessage = RowSet.parseFrom(res.getRowset())
                                    }else {
                                        chats[chatId].receiveMessage = r
                                    }
                                    chats.remove(chatId)
                                }
                                // 清理缓存
                                messageBuffer.remove(messageId)
                            }
                        } catch(Exception e) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private static void startMessageReceiver() {
        // 创建订阅
        clientSub = aeron.addSubscription(CLIENT_URL_PREFIX + instance.host + ":" + instance.port, instance.streamId)

        // 消息处理函数只负责 dispatch，不阻塞
        final FragmentHandler messageHandler = { buffer, offset, length, header ->
            byte[] data = new byte[length]
            buffer.getBytes(offset, data)
            int idx = Math.abs(rr.getAndIncrement() % workers.size())
            workers[idx] << new WorkItem(data)
        }

        // FragmentAssembler 包装
        final FragmentAssembler assembler = new FragmentAssembler(messageHandler)

        // poll 线程单独运行
        Thread.start {
            final IdleStrategy idle = new BackoffIdleStrategy()
            while (running.get()) {
                int fragments = clientSub.poll(assembler, 10)
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
            }
        } catch (Exception e) {
            println "Error sending message: ${e.message}"
            e.printStackTrace()
        }
    }

    void close() {
        println "正在关闭资源..."
        running.set(false)
        try { clientSub?.close() } catch (e) { e.printStackTrace() }
        try { serverPub?.close() } catch (e) { e.printStackTrace() }
        try { aeron?.close() } catch (e) { e.printStackTrace() }
        try { mediaDriver?.close() } catch (e) { e.printStackTrace() }
        println "资源已关闭"
    }
}

