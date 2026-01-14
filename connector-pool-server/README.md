# 🚀 数据库连接池服务端 (Database Connection Pool Server)
<!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
<!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Java-21%2B-orange)
<!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Gradle-8.12%2B-02303A.svg?logo=gradle)

## 📋 目录
+ [项目概述](#-项目概述)
+ [✨ 核心特性](#-核心特性)
+ [🚀 快速开始](#-快速开始)
    - [环境要求](#-环境要求)
    - [构建项目](#-构建项目)
    - [运行服务](#-运行服务)
+ [📊 系统架构](#-系统架构)
+ [🔧 配置说明](#-配置说明)
    - [服务器配置](#服务器配置)
    - [数据库连接池配置](#数据库连接池配置)
+ [📚 API 文档](#-api-文档)
+ [🔍 使用示例](#-使用示例)
+ [⚙️ 性能调优](#️-性能调优)
+ [🔒 安全考虑](#-安全考虑)
+ [🐛 故障排除](#-故障排除)
+ [🤝 贡献指南](#-贡献指南)
+ [📄 许可证](#-许可证)

## 🌟 项目概述
数据库连接池服务器是一个高性能的数据库连接管理中间件，基于 Aeron 框架实现，通过 UDP 协议提供低延迟、高并发的数据库连接管理服务。该服务抽象了数据库连接管理，使应用程序可以通过简单的 API 调用来执行数据库操作，而无需关心底层连接管理细节。

## ✨ 核心特性
+ 🚀 **高性能架构**
    - 基于 Aeron 框架实现，提供微秒级的延迟
    - 多工作线程模型，自动根据 CPU 核心数分配工作线程
    - 零拷贝技术优化网络传输
    - 数据分块传输，支持大数据包的高效传输 (v1.0.1+)
+ 🔌 **智能连接池管理**
    - 基于 HikariCP 实现高效连接池
    - 支持动态添加/移除数据库连接
    - 自动连接验证和泄漏检测
    - 支持连接超时和空闲超时配置
+ 📡 **协议与通信**
    - 基于 UDP 协议，支持高并发请求
    - 请求-响应模型，支持异步处理
    - 内置消息重试机制
+ 🛠️ **数据库支持**
    - 支持所有 JDBC 兼容的数据库
    - 自动连接验证
    - 支持参数化查询和批量操作

## 🆕 版本更新

### v1.0.1 (最新)
+ **新增特性**
  - 数据分块传输：支持大数据包的分块传输，避免单次传输过大的数据包
  - 行级流式处理：查询结果逐行发送，减少内存占用
  - 连接复用：优化连接管理，提高并发处理能力

## 🚀 快速开始
### 环境要求
+ **Java**: 21 或更高版本 (推荐使用 OpenJDK 21 LTS)
+ **Gradle**: 8.12 或更高版本
+ **数据库**: 任何支持 JDBC 的关系型数据库 (MySQL, PostgreSQL, Oracle 等)
+ **网络**: 确保服务端口 (默认 38880/udp) 可访问

### 构建项目


```bash
# 克隆代码仓库
git clone https://github.com/huanhaishijie/tools-collections.git
cd connector-pool-server

# 构建项目
./gradlew clean build
```

### 运行服务
```bash
# 使用默认配置启动服务
java -jar build/libs/connector-pool-server-<version>-all.jar

# 自定义配置启动
java -Dserver.port=48880 \
     -Daeron.mtu.length=16k \
     -Dwork.threads=4 \
     -jar build/libs/connector-pool-server-<version>-all.jar
```

## 📊 系统架构
<!-- 这是一个文本绘图，源码为：graph TD
    A[Client] -->|UDP| B[Connector Pool Server]
    B --> C[(Database 1)]
    B --> D[(Database 2)]
    B --> E[(Database N)]
    
    subgraph Server Components
    B --> F[Aeron Media Driver]
    B --> G[Worker Threads]
    B --> H[Connection Pool Manager]
    B --> I[Request Handler]
    B --> J[Response Handler]
    end -->
![](https://cdn.nlark.com/yuque/__mermaid_v3/f54b8fcac69471d8a2e6c212fa5d54e1.svg)

1. **Aeron Media Driver**：处理高性能消息传递
2. **工作线程池**：处理业务逻辑，线程数默认为 CPU 核心数/2
3. **连接池管理器**：管理数据库连接池的生命周期
4. **请求处理器**：解析并执行数据库操作
5. **响应处理器**：将执行结果序列化并返回给客户端

## 🔧 配置说明
### 服务器配置
| 参数 | 默认值 | 描述 |
| --- | --- | --- |
| `-Dserver.port` | 38880 | 服务监听端口 |
| `-Daeron.mtu.length` | 8k | Aeron MTU 大小 |
| `-Dwork.threads` | CPU核心数/2 | 工作线程数 |
| `-Dlog.level` | INFO | 日志级别 (DEBUG, INFO, WARN, ERROR) |


### 数据库连接池配置
通过 `DataSourceInfo` 消息配置：

| 参数 | 类型 | 必填 | 默认值 | 描述 |
| --- | --- | --- | --- | --- |
| url | string | 是 | - | 数据库连接URL |
| type | string | 是 | - | 数据库驱动类名 |
| username | string | 是 | - | 数据库用户名 |
| password | string | 是 | - | 数据库密码 |
| maxPoolSize | int | 否 | 10 | 最大连接数 |
| minPoolSize | int | 否 | 2 | 最小空闲连接数 |
| idleTimeout | int | 否 | 600000 | 空闲超时(ms) |
| connectionTimeout | int | 否 | 30000 | 连接超时(ms) |
| maxLifetime | int | 否 | 1800000 | 连接最大存活时间(ms) |


## 📚 API 文档
### 1. 注册数据库连接
**请求参数 (DataSourceInfo):**

```json
{
  "url": "jdbc:mysql://localhost:3306/mydb", //数据库连接
  "type": "com.mysql.cj.jdbc.Driver", //数据库驱动
  "username": "user",// 数据库用户名
  "password": "password", // 数据库密码
  "maxPoolSize": 20,
  "minIdle": 5,
  "idleTimeout": 300000,
  "connectionTimeout": 10000,
  "maxLifetime": 1800000,
  "other": //连接池其它配置
  {  
 "cachePrepStmts": "true",
 "prepStmtCacheSize": "250",
 "prepStmtCacheSqlLimit": "2048"
  }
}
```

**响应:**

+ 成功: 返回连接池唯一标识符 (MD5哈希值)
+ 失败: 返回错误信息



### 2. 移除数据源


**请求参数 (DataSourceInfo):**

```json
{
  "url": "jdbc:mysql://localhost:3306/test",
  "type": "com.mysql.cj.jdbc.Driver",
  "username": "user",
  "password": "password",
  "maxPoolSize": 10,
  "minPoolSize": 2,
  "idleTimeout": 600000,
  "connectionTimeout": 30000,
  "maxLifetime": 1800000
}
```

**响应:**

+ 成功:

```json
{
  "code": 0,
  "message":"xxxxxxxxxx",
  "data":"xxxx"
}
```

+ 失败: 返回错误信息

```json
{
  "code": -1, //-1 执行错误，//-2 服务端执行异常
  "message":"xxxxxxxxxx"
}
```

### 3. 执行SQL
**请求参数 (DataSourceInfo.other):**

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
| --- | --- | --- | --- | --- |
| key | string | 是 | - | 连接池标识符 |
| sql | string | 是 | - | SQL查询语句 |
| params | JSON数组 | 否 | [] | SQL参数 |




## ⚙️ 性能调优
1. **连接池配置**

```groovy
// 推荐配置
maxPoolSize = CPU核心数 * 2 + 1
minIdle = CPU核心数 / 2
idleTimeout = 60000  // 1分钟
maxLifetime = 1800000  // 30分钟
leakDetectionThreshold = 60000  // 1分钟
```

2. **Aeron 配置**

```bash
# 增加MTU大小（需要网络支持）
-Daeron.mtu.length=16k

# 调整接收窗口大小
-Daeron.socket.so_rcvbuf=2097152
-Daeron.socket.so_sndbuf=2097152
```

3. **JVM 调优**

```bash
-Xms2g -Xmx2g  # 堆内存
-XX:+UseG1GC   # G1垃圾收集器
-XX:MaxGCPauseMillis=200
```

## 🔒 安全考虑
1. **网络传输安全**
    - 建议在网络层面使用 VPN 或专用网络
    - 考虑使用 IPSec 或 WireGuard 加密通信
2. **认证授权**
    - 实现应用层认证机制
    - 使用最小权限原则配置数据库用户
3. **敏感信息**
    - 避免在日志中记录敏感信息
    - 使用环境变量或配置中心管理凭据

## 🐛 故障排除
### 常见问题
1. **连接泄漏**
    - 检查 `leakDetectionThreshold` 设置
    - 确保所有连接在使用后正确关闭
2. **性能问题**
    - 监控连接池使用情况
    - 检查慢查询
    - 调整工作线程数
3. **网络问题**
    - 检查防火墙设置
    - 验证 MTU 大小设置

## 🤝 贡献指南
欢迎提交 Issue 和 Pull Request。请确保：

1. 代码符合 Google Java 代码风格
2. 提交信息遵循 Conventional Commits 规范
3. 新功能需包含测试用例

## 📄 许可证
本项目采用 [Apache License 2.0](LICENSE) 开源协议。

# Aeron 配置
aeron.mtu.length=8k  
aeron.threading.mode=SHARED  
aeron.idle.strategy=sleeping

# 日志配置
logging.level.root=INFO  
logging.level.com.yuezm=DEBUG

```plain

### 数据库连接池配置

支持通过 `DataSourceInfo` 消息配置 HikariCP 连接池参数：

| 参数 | 默认值 | 描述 |
|------|--------|------|
| `maxPoolSize` | 10 | 连接池最大连接数 |
| `minIdle` | 2 | 连接池最小空闲连接数 |
| `idleTimeout` | 600000 | 连接空闲超时时间(毫秒) |
| `connectionTimeout` | 30000 | 连接超时时间(毫秒) |
| `maxLifetime` | 1800000 | 连接最大存活时间(毫秒) |
| `autoCommit` | true | 是否自动提交事务 |
| `leakDetectionThreshold` | 0 | 连接泄漏检测阈值(毫秒) |
| `validationTimeout` | 5000 | 连接验证超时时间(毫秒) |

**示例配置:**

```json
{
  "url": "jdbc:mysql://localhost:3306/mydb",
  "type": "com.mysql.cj.jdbc.Driver",
  "username": "user",
  "password": "password",
  "maxPoolSize": 20,
  "minIdle": 5,
  "idleTimeout": 300000,
  "connectionTimeout": 10000,
  "maxLifetime": 1800000,
  "other": {
 "cachePrepStmts": "true",
 "prepStmtCacheSize": "250",
 "prepStmtCacheSqlLimit": "2048"
  }
}
```

## 🔍 使用示例
### Java 客户端示例
```java
// 创建 Aeron 客户端
Context ctx = new Aeron.Context()
    .aeronDirectoryName("/path/to/aeron");

try (Aeron aeron = Aeron.connect(ctx);
     Publication publication = aeron.addPublication("aeron:udp?endpoint=server:38880", 2500);
     Subscription subscription = aeron.addSubscription("aeron:udp?endpoint=client:0", 2501)) {
    
    // 1. 注册数据库连接
    DataSourceInfo dsInfo = DataSourceInfo.newBuilder()
        .setUrl("jdbc:mysql://localhost:3306/test")
        .setType("com.mysql.cj.jdbc.Driver")
        .setUsername("user")
        .setPassword("password")
        .setMaxPoolSize(10)
        .build();
        
    // 发送请求并接收响应...
    
    // 2. 执行查询
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("key", "connection_key");
    queryParams.put("sql", "SELECT * FROM users WHERE id = ?");
    queryParams.put("params", "[1]");
    dsInfo.setOther(queryParams)
    
    // 发送查询请求并处理结果...
    publication.offer(dsInfo.getBeatys())
    
}
```

### 性能测试
使用 `wrk` 进行基准测试：

```bash
# 启动服务
java -jar connector-pool-server.jar &

# 运行性能测试
wrk -t4 -c100 -d30s --latency -s test/benchmark.lua http://localhost:38880/
```

## ⚙️ 性能调优
1. **Aeron 调优**:
    - 调整 `aeron.mtu.length` 以适应网络 MTU
    - 配置适当的 `aeron.term.buffer.length` 和 `aeron.ipc.term.buffer.length`
    - 根据 CPU 核心数调整工作线程数
2. **连接池调优**:
    - 根据数据库负载调整 `maxPoolSize` 和 `minIdle`
    - 设置合理的 `idleTimeout` 和 `maxLifetime`
    - 启用连接泄漏检测 `leakDetectionThreshold=60000`
3. **JVM 调优**:

```bash
-Xms2g -Xmx2g \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=100 \
-XX:+UseStringDeduplication
```

## 🔒 安全考虑
1. **网络传输安全**:
    - 在生产环境中使用 TLS 加密 UDP 通信
    - 配置防火墙规则，限制访问来源 IP
    - 使用 VPN 或私有网络部署
2. **认证授权**:
    - 实现 API 密钥认证
    - 限制敏感操作的访问权限
    - 记录所有操作日志
3. **数据安全**:
    - 加密存储数据库凭据
    - 使用最小权限原则配置数据库用户
    - 定期轮换数据库密码

## 🐛 故障排除
### 常见问题
1. **连接泄漏**
    - 现象: 连接数持续增长不释放
    - 解决方案: 检查代码中是否正确关闭了连接，设置 `leakDetectionThreshold`
2. **连接超时**
    - 现象: `ConnectionTimeoutException`
    - 解决方案: 增加 `connectionTimeout`，检查网络连接
3. **性能下降**
    - 现象: 响应时间变长
    - 解决方案: 检查数据库负载，调整连接池参数

### 日志分析
日志文件位于 `logs/connector-pool-server.log`，常见日志级别：

+ `ERROR`: 需要立即处理的错误
+ `WARN`: 潜在问题，需要关注
+ `INFO`: 一般信息，记录重要操作
+ `DEBUG`: 调试信息，记录详细执行过程




## 🤝 贡献指南
欢迎贡献代码！请遵循以下步骤：

1. Fork 项目并创建特性分支 (`git checkout -b feature/amazing-feature`)
2. 提交更改 (`git commit -m 'Add some amazing feature'`)
3. 推送到分支 (`git push origin feature/amazing-feature`)
4. 提交 Pull Request

### 开发环境
1. 安装 JDK 21 和 Gradle 8.12
2. 克隆代码: `git clone https://github.com/yourusername/connector-pool-server.git`
3. 导入到 IDE (推荐 IntelliJ IDEA)
4. 运行测试: `./gradlew test`

### 代码规范
+ 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
+ 使用 4 个空格缩进
+ 类名使用大驼峰，方法名使用小驼峰
+ 添加有意义的注释和文档

## 📄 许可证
本项目采用 [Apache License 2.0](LICENSE) 开源协议。

## 👥 作者
+ **yuezm** - 项目地址 - [GitHub](https://github.com/huanhaishijie/tools-collections)



## 🙏 致谢
+ [Aeron](https://github.com/real-logic/aeron) - 高性能消息传输
+ [HikariCP](https://github.com/brettwooldridge/HikariCP) - 高性能 JDBC 连接池
+ [Protocol Buffers](https://developers.google.com/protocol-buffers) - 高效数据序列化

