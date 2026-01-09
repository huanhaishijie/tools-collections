package com.zz.datacenter

import com.yuezm.project.connector.proto.*


/**
 * Test
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/17 9:08
 */
class Test {

    static String CLIENT_URL_PREFIX = "aeron:udp?endpoint="
    static def testExecSql() {
        String key = "18249934f139d9671b7a5f76336b6cfd"
        Client client = null
        client = Client.getInstance "127.0.0.1", 38881, 2500, "127.0.0.1"
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
                                .setReplyStream(2500)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key).putOther("exec", "rows").putOther("sql", "show tables").build()

        // 发送消息
        client.send dataSourceInfo, { backInfo ->
            println "Received response: ${backInfo}"
        }
        // 保持程序运行，等待用户输入后退出
        println "程序运行中，按 Enter 键退出..."
        System.in.read()
        client.close()

    }

    static def registerDb() {

        Client client = null
        client = Client.getInstance "127.0.0.1", 38881, 2500, "127.0.0.1"
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
        client.close()

    }




    static void main(String[] args) {
//        registerDb()
        testExecSql()

    }
}
