package com.yuezm.project.connector

import com.yuezm.project.connector.proto.DataSourceInfo
import io.aeron.Aeron
import io.aeron.Publication
import io.aeron.Subscription
import io.aeron.driver.MediaDriver
import io.aeron.logbuffer.FragmentHandler
import org.agrona.concurrent.BackoffIdleStrategy
import org.agrona.concurrent.IdleStrategy
import org.agrona.concurrent.UnsafeBuffer
import com.yuezm.project.connector.proto.Response

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/10 17:06
 */
class Application {
    private static int port = 38880
    private static String serverUrl = "aeron:udp?endpoint=0.0.0.0:$port"
    private static int streamId = 2500
    private final static AtomicBoolean running = new AtomicBoolean(true)
    private static Map<String, Publication> pubs = new ConcurrentHashMap<>()

    static void main(String[] args) {
        println "Starting server on $serverUrl, streamId: $streamId"

        final IdleStrategy idle = new BackoffIdleStrategy()
        Runtime.runtime.addShutdownHook(new Thread({
            println "Shutting down server..."
            running.set(false)
        }))

        MediaDriver mediaDriver = MediaDriver.launchEmbedded()
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()))
        Subscription sub = aeron.addSubscription(serverUrl, streamId)
        DBServer server = new DBServer()

        final FragmentHandler handler = { buffer, offset, length, header ->
            try {
                byte[] data = new byte[length]
                buffer.getBytes(offset, data)
                println "Received message, length: $length"
                // 解析 DataSourceInfo 消息
                DataSourceInfo info = DataSourceInfo.parseFrom(data)
                println "Parsed DataSourceInfo: $info"
                // 处理消息
                Response resp = server.handle(data)
                resp = resp.toBuilder().setExec(info.exec).build()
                println "Generated response: $resp"
                // 发送响应
                if (resp.hasExec() && resp.exec.hasRequestInfo()) {
                    def ri = resp.exec.requestInfo
                    String channel = ri.replyChannel
                    int replyStream = ri.replyStream
                    String key = "$channel:$replyStream"

                    println "Sending response to $key"
                    Publication pub = pubs.computeIfAbsent(key, k -> {
                        println "Creating new publication for $k"
                        aeron.addPublication(channel, replyStream)
                    })

                    byte[] out = resp.toByteArray()
                    UnsafeBuffer outBuf = new UnsafeBuffer(out)
                    try {
                        long result = pub.offer(outBuf, 0, out.length)

                        if (result < 0) {
                            println "Failed to send response, result: $result"
                        } else {
                            println "Response sent successfully"
                        }
                    }catch (Exception e){
                        e.printStackTrace()
                        println "Failed to send response, error: ${e.message} info: $info"
                    }

                }
            } catch (Exception e) {
                println "Error processing message: ${e.message}"
                e.printStackTrace()
            }
        }

        try {
            println "Server started, waiting for messages..."
            while (running.get()) {
                int fragments = sub.poll(handler, 10)
                idle.idle(fragments)
            }
        } finally {
            println "Closing resources..."
            sub.close()
            aeron.close()
            mediaDriver.close()
            pubs.each { k, v -> v.close() }
            println "Server stopped"
        }
    }
}
