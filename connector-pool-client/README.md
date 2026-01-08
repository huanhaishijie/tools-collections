# Connector Pool Client (v1.0.1)

## 项目简介

基于 Aeron 的高性能数据库连接池客户端，使用 Protocol Buffers 进行高效消息序列化，支持异步消息通信。

## 功能特性

- 🚀 基于 Aeron 的高性能消息通信
- 🔄 异步非阻塞 I/O 操作
- 🛡️ 线程安全的客户端实现
- 📦 支持多种数据库操作
- ⚡ 可配置的连接池参数
- 📡 支持远程数据库连接管理
- 🎯 1.0.1 新特性：支持大数据集的分片传输与自动重组
  - 自动处理大数据集的分片接收
  - 支持流式处理查询结果
  - 优化内存使用，降低大结果集的内存占用
  - 自动处理分片顺序和完整性校验

## 快速开始

### 环境要求

- JDK 1.8+
- Gradle 8.0+
- Aeron 消息系统

### 安装

```bash
git clone https://github.com/yourusername/connector-pool-client.git
cd connector-pool-client
```

### 构建项目

```bash
gradle build
```

## 使用示例


引入依赖

```xml
<dependency>
  <groupId>com.yuezm.project.connector</groupId>
  <artifactId>connector-pool-client</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.google.protobuf</groupId>
  <artifactId>protobuf-java</artifactId>
  <version>3.24.4</version>
</dependency>
```

### 1. 初始化客户端

```groovy
def client = Client.getInstance("127.0.0.1", 38881, 2500, "127.0.0.1")
```

#### 1.1 参数配置
Client 属性

host: client ip地址

port: 客户端接收端口

streamId:客户端接收流id

serverHost : 服务端地址

### 2. 注册数据库连接
groovy 代码：

```groovy
def registerInfo = DataSourceInfo.newBuilder().setExec(
    ExecInfo.newBuilder().setRequestInfo(
        RequestInfo.newBuilder()
            .setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
            .setReplyStream(2500)
            .build()
    ).setMethod("register").build()
).setType("com.mysql.cj.jdbc.Driver")
                .setUsername("root")
                .setPassword("skzz@2021")
                .setMaxPoolSize(10)
                .setMinPoolSize(1)
                .setIdleTimeout(60000)
                .setConnectionTimeout(30000)
 .build()

client.send(registerInfo) { response ->
    println "数据库注册结果: ${response}"
}
```

java 代码：

```java
// First, create the RequestInfo
RequestInfo requestInfo = RequestInfo.newBuilder()
.setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
.setReplyStream(2500)
.build();

// Then create the ExecInfo with the RequestInfo
ExecInfo execInfo = ExecInfo.newBuilder()
.setRequestInfo(requestInfo)
.setMethod("register")
.build();

// Finally, build the DataSourceInfo with all parameters
DataSourceInfo registerInfo = DataSourceInfo.newBuilder()
.setExec(execInfo)
.setType("com.mysql.cj.jdbc.Driver")
                .setUsername("root")
                .setPassword("skzz@2021")
                .setMaxPoolSize(10)
                .setMinPoolSize(1)
                .setIdleTimeout(60000)
                .setConnectionTimeout(30000)
.build();

// Send the request with callback
client.send(registerInfo, new Closure(this){{
    @Override
    Object call(){
        println "数据库注册结果: ${response}"//业务逻辑
    }
}});
```



### 3. 执行SQL查询
```groovy
def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
        ExecInfo.newBuilder().setRequestInfo(
                RequestInfo.newBuilder()
                        .setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
                        .setReplyStream(2500)
                        .build()
        ).setMethod("execSql").build()
).putOther("key", "your-db-key")
        .putOther("exec", "rows")
        .putOther("sql", "SELECT * FROM your_table")
        .build()

client.send(dataSourceInfo) { response ->
  println "查询结果: ${response}"
}
```

### 4. 大数据集流式处理

1.0.1 版本支持大数据集的流式处理，自动处理分片传输：

```groovy
def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
    ExecInfo.newBuilder().setRequestInfo(
        RequestInfo.newBuilder()
            .setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
            .setReplyStream(2500)
            .build()
    ).setMethod("execSqlStream").build()
).putOther("key", "your-db-key")
 .putOther("sql", "SELECT * FROM large_table")
 .build()

// 处理流式结果
client.send(dataSourceInfo) { response ->
    if (response instanceof RowSet) {
        // 处理流式数据块
        response.rowsList.each { row ->
            // 处理每一行数据
            def values = row.valuesList.collect { it.stringValue }
            println "处理行: ${values}"
        }
    } else if (response == "stream_end") {
        println "流式处理完成"
    } else {
        println "处理结果: ${response}"
    }
}
```
### 5. 更多信息观看
[README.md](../field-filter/src/main/groovy/com/yuezm/project/sql/README.md)

## API 参考

### Client 类

#### 方法

- `static Client getInstance(String host, int port, int streamId, String serverHost, String model = "local", String shell = "")`
  获取客户端实例（单例模式）
  
- `void send(DataSourceInfo dataSourceInfo, Closure callback)`
  发送请求到服务器
  
- `void close()`
  关闭客户端，释放资源

## 配置说明

### 客户端配置参数

| 参数名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| host | String | "127.0.0.1" | 客户端主机地址 |
| port | int | 38881 | 客户端端口 |
| streamId | int | 2500 | 流ID |
| serverHost | String | "127.0.0.1" | 服务器主机地址 |
| model | String | "local" | 运行模式 |
| shell | String | "" | 命令行参数 |

## 开发指南

### 构建项目

```bash
# 编译项目
gradle compileGroovy

# 运行测试
gradle test

# 构建JAR包
gradle jar
```

## 许可证

MIT

## 贡献

欢迎提交 Issue 和 Pull Request
