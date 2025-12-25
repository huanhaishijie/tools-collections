# Connector Pool Client

## 项目简介

基于 Aeron 的高性能数据库连接池客户端，使用 Protocol Buffers 进行高效消息序列化，支持异步消息通信。

## 功能特性

- 🚀 基于 Aeron 的高性能消息通信
- 🔄 异步非阻塞 I/O 操作
- 🛡️ 线程安全的客户端实现
- 📦 支持多种数据库操作
- ⚡ 可配置的连接池参数
- 📡 支持远程数据库连接管理

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

### 1. 初始化客户端

```groovy
def client = Client.getInstance("127.0.0.1", 38881, 2500, "127.0.0.1")
```

### 2. 执行SQL查询

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

### 3. 注册数据库连接

```groovy
def registerInfo = DataSourceInfo.newBuilder().setExec(
    ExecInfo.newBuilder().setRequestInfo(
        RequestInfo.newBuilder()
            .setReplyChannel("aeron:udp?endpoint=127.0.0.1:38881")
            .setReplyStream(2500)
            .build()
    ).setMethod("register").build()
).putOther("key", "your-db-key")
 .putOther("url", "jdbc:mysql://localhost:3306/your_database")
 .putOther("username", "db_user")
 .putOther("password", "db_password")
 .putOther("driverClassName", "com.mysql.cj.jdbc.Driver")
 .build()

client.send(registerInfo) { response ->
    println "数据库注册结果: ${response}"
}
```
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
