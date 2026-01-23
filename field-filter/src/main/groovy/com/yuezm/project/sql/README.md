# SQL 工具库

一个基于 Groovy 的 SQL 工具库，提供数据库连接池管理、SQL 构建和模板处理功能。

## 功能特性

### 1. 核心组件

#### SqlHandler
- 数据库操作基类
- 支持基本的 SQL 执行
- 提供表结构操作接口
- 支持字段类型映射

#### SqlPoolHandler
- 基于连接池的数据库操作
- 支持 Aeron 高性能消息通信
- 自动连接管理
- 线程安全

#### SqlLocalPoolHandler
- 基于 HikariCP 的本地连接池基类
- 继承 SqlHandler 的所有功能
- 提供线程安全的连接池管理
- 支持连接池监控和性能统计
- 自动连接验证和泄漏检测

#### 多数据库支持
- PostgreSQL
- MySQL
- Oracle
- SQL Server
- 其他兼容 JDBC 的数据库

### 2. SQL 构建器 (SqlBuilder)

#### 查询构建
- 基于 Wrapper 的构建器模式
- 支持字段选择和表名配置
- 支持单表和多表查询
- 灵活的条件构建机制

#### 条件构建
- 自定义条件构建器 (ConditionBuilder)
- 支持参数化条件
- 灵活的 SQL 片段组合
- 字段名自动转义（数据库特定）

### 3. SQL 模板引擎 (SqlTemplateEngine)

#### 模板语法
- 支持 Groovy 脚本
- 条件判断 (#if/#elseif/#else)
- 循环遍历 (#each)
- 变量插值 (${var} 或 :var)

#### 安全特性
- 沙箱执行环境
- 脚本执行控制
- 资源访问限制

### 4. 本地化连接池 (Local Connection Pool)

#### SqlLocalPoolHandler
基于 HikariCP 的高性能本地连接池实现，提供线程安全的数据库连接管理。

**核心特性**
- 使用 HikariCP 作为连接池实现
- 支持连接池缓存和复用
- 自动连接验证和泄漏检测
- 线程安全的并发访问
- 支持多种数据库类型
- **实时连接池监控**
- **完整的性能统计**

**监控方法**
```groovy
// 获取连接池状态信息
def status = handler.getPoolStatus()
println "连接池名称: ${status.poolName}"
println "总连接数: ${status.totalConnections}"
println "活跃连接数: ${status.activeConnections}"
println "空闲连接数: ${status.idleConnections}"
println "等待线程数: ${status.waitingThreads}"

// 打印格式化的连接池状态
handler.printPoolStatus()
```

**支持的数据库实现**

##### PostgreSQL 本地连接池 (PGSqlLocalPool)
```groovy
import com.yuezm.project.sql.pg.PGSqlLocalPool
import com.yuezm.project.sql.DatasourceProperties

// 创建数据源配置
def properties = new DatasourceProperties(
    url: "jdbc:postgresql://localhost:5432/mydb?stringtype=unspecified&currentSchema=public",
    username: "postgres",
    password: "password",
    driverClassName: "org.postgresql.Driver"
)

// 创建本地连接池处理器
def handler = new PGSqlLocalPool(properties, [
    maxPoolSize: 20,
    minPoolSize: 5,
    connectionTimeout: 30000,
    idleTimeout: 600000
])

// 执行查询
def result = handler.firstRow("SELECT * FROM users WHERE id = ?", [1])
println result

// 批量操作
def batchParams = [
    [name: '张三', email: 'zhangsan@example.com'],
    [name: '李四', email: 'lisi@example.com']
]

handler.withBatch(10, 'INSERT INTO users (name, email) VALUES (:name, :email)') { ps ->
    batchParams.each { params ->
        ps.addBatch(params)
    }
}

// 监控连接池状态
def status = handler.getPoolStatus()
println "活跃连接数: ${status.activeConnections}"
println "空闲连接数: ${status.idleConnections}"

// 关闭连接池
handler.close()
```

##### MySQL 本地连接池 (MysqlLocalPool)
```groovy
import com.yuezm.project.sql.mysql.MysqlLocalPool
import com.yuezm.project.sql.DatasourceProperties

// 创建数据源配置
def properties = new DatasourceProperties(
    url: "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC",
    username: "root",
    password: "password",
    driverClassName: "com.mysql.cj.jdbc.Driver"
)

// 创建本地连接池处理器
def handler = new MysqlLocalPool(properties, [
    maxPoolSize: 20,
    minPoolSize: 5,
    connectionTimeout: 30000,
    idleTimeout: 600000
])

// 执行查询
def users = handler.rows("SELECT * FROM users WHERE status = ?", ['active'])
println users

// 获取表信息
def tableInfo = handler.getTableInfo("users", "mydb")
println "表注释: ${tableInfo.comment}"
println "字段信息: ${tableInfo.fields}"

// 监控连接池状态
def status = handler.getPoolStatus()
println "总连接数: ${status.totalConnections} / ${status.maxPoolSize}"
println "连接池使用率: ${(status.activeConnections * 100 / status.maxPoolSize).round(1)}%"

// 关闭连接池
handler.close()
```

#### 连接池配置参数

```groovy
Map<String, Object> poolConfig = [
    // HikariCP 核心配置
    maxPoolSize: 20,              // 最大连接数 (对应 maximumPoolSize)
    minPoolSize: 5,               // 最小空闲连接数 (对应 minimumIdle)  
    connectionTimeout: 30000,     // 连接超时时间(毫秒) (对应 connectionTimeout)
    idleTimeout: 600000,          // 空闲连接超时时间(毫秒) (对应 idleTimeout)
    
    // HikariCP 高级配置（可选，覆盖默认值）
    maxLifetime: 1800000,         // 连接最大生存时间(毫秒)，默认30分钟
    leakDetectionThreshold: 60000, // 连接泄漏检测阈值(毫秒)，默认60秒
    validationTimeout: 5000,     // 连接验证超时(毫秒)，默认5秒
    connectionInitSql: "SELECT 1", // 连接初始化SQL
    poolName: "MyHikariPool",     // 连接池名称
    
    // 数据源特定属性（通过 addDataSourceProperty 设置）
    cachePrepStmts: "true",
    prepStmtCacheSize: "250", 
    prepStmtCacheSqlLimit: "2048",
    useServerPrepStmts: "true"
]
```

**HikariCP 核心配置说明：**
- `maxPoolSize`: 最大连接池大小，建议 10-20 之间
- `minPoolSize`: 最小空闲连接数，建议与 maxPoolSize 相同或略小
- `connectionTimeout`: 获取连接的最大等待时间，建议 30000ms (30秒)
- `idleTimeout`: 空闲连接的超时时间，建议 600000ms (10分钟)

**可选的高级配置：**
- `maxLifetime`: 连接最大生存时间，超过此时间将被回收
- `leakDetectionThreshold`: 连接泄漏检测阈值，检测未关闭的连接
- `validationTimeout`: 连接验证超时时间
- `connectionInitSql`: 连接创建时执行的初始化 SQL
- `poolName`: 连接池名称，用于监控和日志

**数据库特定属性：**
- **MySQL**: `cachePrepStmts`, `useSSL`, `serverTimezone` 等
- **PostgreSQL**: `stringtype`, `prepareThreshold`, `binaryTransferEnable` 等
- **通用**: `socketTimeout`, `loginTimeout`, `queryTimeout` 等

**性能优化建议：**
```groovy
// 高性能配置示例
Map<String, Object> highPerfConfig = [
    maxPoolSize: 20,
    minPoolSize: 10,
    connectionTimeout: 30000,
    idleTimeout: 300000,          // 5分钟
    maxLifetime: 900000,          // 15分钟
    
    // PostgreSQL 高性能配置
    prepareThreshold: "5",
    binaryTransferEnable: "true",
    reWriteBatchedInserts: "true",
    
    // MySQL 高性能配置
    cachePrepStmts: "true",
    prepStmtCacheSize: "500",
    prepStmtCacheSqlLimit: "4096",
    useServerPrepStmts: "true",
    rewriteBatchedStatements: "true",
    
    // 网络优化
    socketTimeout: "60000",
    tcpKeepAlive: "true",
    useUnicode: "true",
    characterEncoding: "UTF-8"
]
```

#### 高级功能

##### 表结构操作
```groovy
// 获取表信息
def tableInfo = handler.getTableInfo("users", "schema_name")
println "表名: ${tableInfo.tableName}"
println "表注释: ${tableInfo.comment}"
tableInfo.fields.each { field ->
    println "字段: ${field.colName}, 类型: ${field.dataType}, 注释: ${field.comment}"
}

// 获取主键信息
def primaryKeys = handler.getTablePrimarys("users", "schema_name")
println "主键字段: ${primaryKeys.COLUMN_NAME}"

// 获取表数据容量
def tableSize = handler.getTableDataCapacity("users", "schema_name")
println "表大小: ${tableSize} bytes"

// 添加表注释
handler.addTableMemo("users", "用户表")

// 删除表
handler.dropTable("users")
```

##### 数据类型映射
本地连接池支持 Java 类型到 SQL 类型的自动映射：

**PostgreSQL 类型映射**
- Integer → int4
- Long → int8
- String → varchar(1024)
- Date/LocalDateTime → timestamp
- List/Map → jsonb
- UUID → uuid
- byte[] → bytea

**MySQL 类型映射**
- Integer → INT
- Long → BIGINT
- String → VARCHAR(1024)
- Date/LocalDateTime → TIMESTAMP
- List/Map → JSON
- UUID → uuid
- byte[] → BLOB

##### 连接池监控

本地连接池提供了完整的监控功能，可以实时查看连接池的使用情况。

```groovy
// 获取连接池状态信息
def status = handler.getPoolStatus()
println "连接池名称: ${status.poolName}"
println "总连接数: ${status.totalConnections} / ${status.maxPoolSize}"
println "活跃连接数: ${status.activeConnections}"
println "空闲连接数: ${status.idleConnections}"
println "等待线程数: ${status.waitingThreads}"

// 打印格式化的连接池状态
handler.printPoolStatus()
```

**输出示例：**
```
=== Connection Pool Status ===
🏊 Pool Name: HikariPool-1
📊 Connection Statistics:
   Total Connections: 12 / 20
   Active Connections: 8 🔥
   Idle Connections: 4 💤
   Waiting Threads: 0 ⏳
   Pool Usage: 40.0% (active), 60.0% (total)
   Status: 🟢 Active
===============================
```

**监控指标说明：**
- **Total Connections**: 当前总连接数
- **Active Connections**: 正在使用的连接数
- **Idle Connections**: 空闲可用连接数
- **Waiting Threads**: 等待获取连接的线程数
- **Pool Usage**: 连接池使用率
- **Status**: 连接池状态

##### 并发测试示例
```groovy
// 多线程并发测试
def properties = new DatasourceProperties(
    url: "jdbc:postgresql://localhost:5432/mydb",
    username: "postgres",
    password: "password",
    driverClassName: "org.postgresql.Driver"
)

def handler = new PGSqlLocalPool(properties, [maxPoolSize: 20, minPoolSize: 10])

println "=== 初始连接池状态 ==="
handler.printPoolStatus()

println "开始并发测试..."
def threads = []
20.times { i ->
    def thread = Thread.start {
        try {
            def results = handler.rows("SELECT * FROM users WHERE id = ?", [i + 1])
            println "Thread ${Thread.currentThread().getName()} 查询到 ${results.size()} 条记录"
        } catch (Exception e) {
            println "Thread ${Thread.currentThread().getName()} 查询失败: ${e.message}"
        }
    }
    threads << thread
}

// 等待所有线程完成
threads.each { it.join() }

println "=== 测试结束后的连接池状态 ==="
handler.printPoolStatus()

// 等待一段时间观察连接回收
println "等待连接回收..."
sleep(10000)

println "=== 连接回收后的连接池状态 ==="
handler.printPoolStatus()

handler.close() // 关闭整个连接池
```

### 5. 实用工具

#### 表结构操作
- 表创建/删除
- 字段管理
- 表注释
- 表数据量统计

#### 批量操作
- 批量插入/更新
- 批量删除
- 批量查询

## 快速开始

### 环境要求
- Java 8 或更高版本
- Groovy
- 相关数据库驱动（如 PostgreSQL、MySQL 等）

### 添加依赖

```gradle
dependencies {
    implementation 'com.yzm:field-filter:1.0.0'
    // 添加数据库驱动，例如 PostgreSQL
    implementation 'org.postgresql:postgresql:42.3.1'
}
```

### 基本使用

#### 1. 本地连接池方式（推荐）

##### SqlLocalPoolHandler 基类使用
```groovy
import com.yuezm.project.sql.SqlLocalPoolHandler
import com.yuezm.project.sql.DatasourceProperties

// 创建数据源配置
def properties = new DatasourceProperties(
    url: "jdbc:postgresql://localhost:5432/mydb",
    username: "postgres",
    password: "password",
    driverClassName: "org.postgresql.Driver"
)

// 使用基类（需要实现具体的数据库操作）
def handler = new SqlLocalPoolHandler(properties, [maxPoolSize: 20, minPoolSize: 5]) {
    // 实现数据库特定的操作
    def getWrapper() {
        // 返回数据库特定的 Wrapper
        return new PGSqlWrapper()
    }
}

// 监控连接池状态
handler.printPoolStatus()

// 执行查询
def results = handler.rows("SELECT * FROM users")
println results

// 关闭连接池
handler.close()
```

##### PostgreSQL 本地连接池
```groovy
import com.yuezm.project.sql.pg.PGSqlLocalPool
import com.yuezm.project.sql.DatasourceProperties

// 创建数据源配置
def properties = new DatasourceProperties(
    url: "jdbc:postgresql://localhost:5432/mydb?stringtype=unspecified&currentSchema=public",
    username: "postgres",
    password: "password",
    driverClassName: "org.postgresql.Driver"
)

// 创建本地连接池处理器
def handler = new PGSqlLocalPool(properties, [
    maxPoolSize: 20,
    minPoolSize: 5,
    connectionTimeout: 30000,
    idleTimeout: 600000
])

// 执行查询
def result = handler.firstRow("SELECT * FROM users WHERE id = ?", [1])
println result

// 批量操作
def batchParams = [
    [name: '张三', email: 'zhangsan@example.com'],
    [name: '李四', email: 'lisi@example.com']
]

handler.withBatch(10, 'INSERT INTO users (name, email) VALUES (:name, :email)') { ps ->
    batchParams.each { params ->
        ps.addBatch(params)
    }
}

// 关闭连接池
handler.close()
```

##### MySQL 本地连接池
```groovy
import com.yuezm.project.sql.mysql.MysqlLocalPool
import com.yuezm.project.sql.DatasourceProperties

// 创建数据源配置
def properties = new DatasourceProperties(
    url: "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC",
    username: "root",
    password: "password",
    driverClassName: "com.mysql.cj.jdbc.Driver"
)

// 创建本地连接池处理器
def handler = new MysqlLocalPool(properties, [
    maxPoolSize: 20,
    minPoolSize: 5,
    connectionTimeout: 30000,
    idleTimeout: 600000
])

// 执行查询
def users = handler.rows("SELECT * FROM users WHERE status = ?", ['active'])
println users

// 获取表信息
def tableInfo = handler.getTableInfo("users", "mydb")
println "表注释: ${tableInfo.comment}"
println "字段信息: ${tableInfo.fields}"

// 关闭连接池
handler.close()
```

#### 2. 分布式连接池方式（Aeron）

```groovy
import com.yuezm.project.sql.*
static void main(String[] args) {

   /**
    * 数据源连接池复用服务 框架
    * Aeron 是一个为低延迟、高吞吐、可预测性能而设计的消息传输框架，用来在进程之间或机器之间高效传输字节流。
    *   1.RPC / 请求响应
    *  2. SQL 执行代理
    *  3. 游戏服务器
    *  4. 实时风控
    *  5. 撮合引擎
    *  6. 低延迟微服务通信
    *  7.本地多进程通信（IPC）
    * Protocol 统一通信语义（语法与语义约定）
    * 1. 序列化体积小
    * 二进制编码，相比 JSON / XML 通常 小 3～10 倍
    * 使用 Varint 对整数进行压缩
    * 不携带字段名，仅使用 field number
    * 2.生成代码，无反射（或极少反射）生成代码，无反射（或极少反射）
    * 结构化内存布局，CPU Cache 友好
    * 在高并发、低延迟场景（RPC、消息系统）中：
    * 明显优于 JSON、XML
    * 通常略慢于极致手写二进制协议，但可维护性远高
    * 3.明确的 Schema（IDL）
    * 通过 .proto 文件： 明确字段类型
    * 明确字段编号（tag）
    * 明确必填 / 可选 / repeated
    * 这带来：
    * 编译期校验
    * 消除"弱约束 JSON"的隐性 Bug
    */
   def properties = new DatasourceProperties(
           url: "jdbc:postgresql://192.168.111.244:35432/gis_db?stringtype=unspecified&currentSchema=test",
           username: "postgres",
           password: "skzz@123",
           driverClassName: "org.postgresql.Driver")

   /**
    * 1. 配置服务端连接信息
    * clientHost 当前服务接收地址
    * clientPort 当前服务接收端口
    * clientSteamId 当前服务接收流id
    * serverHost 服务端地址
    */
   PoolConfig.instance.clientHost = "127.0.0.1"
   PoolConfig.instance.clientPort = 38881
   PoolConfig.instance.clientSteamId = 2500
   PoolConfig.instance.serverHost = "127.0.0.1"
   /**
    * 2. 注册数据源
    *  创建实例会自动向服务端注册数据源，如果服务端这个数据被注册了，
    *  服务端不会重新注册数据源，会直接返回key,key会保存在handler中
    */
   def handler = new PGSqlPoolHandler(properties)
   /**
    * 3.使用handler执行sql
    */
   def res = handler.firstRow("select * from test1 where id = ? ", ['1'])
   println "更新之前：res.name:${res?.name}"
   handler.execute([id: '1', name: "6sdfsfd"], "update test1 set name = :name where id = :id")
   res = handler.firstRow([id:'1'], "select * from test1 where id = :id ")
   println "更新之后：res.name:${res?.name}"
   /**
    * 4.使用查询功能,自定义返回结果（里面能用statement 和rowset,高度自定义）
    * 4.1 内部参数-固定参数
    *     (1).sqlHandler,sql执行器，具体使用参照groovy.sql.Sql
    *     (2).JSON 可以序列化对象，已经特殊处理，不会把中文转成unicode,不可反序列化对象, 具体使用参考groovy.json.JsonGenerator
    *     (3).sqlStr, 传入的sql
    * 4.2 内部参数-可变参数
    *      (1)args. 用户传入单个参数，使用 args[0]能获取到参数
    *      (2)args. 用户传入集合参数，使用 args 得到这个集合参数
    *      (3)任意参数，用户传入obj,如下案例，直接使用里面key(properties)
    */
   def res2 = handler.rows([id:'1'],"select * from test1 where id = :id", """
            sqlHandler.rows(['id': id], sqlStr)
""")
   println res2
}
```

#### 3. 执行查询

```groovy
// 查询单条记录
def user = handler.firstRow('SELECT * FROM users WHERE id = ?', [1])

// 查询多条记录
def activeUsers = handler.rows('SELECT * FROM users WHERE status = ?', ['active'])

// 执行更新
def updated = handler.executeUpdate('UPDATE users SET status = ? WHERE id = ?', ['inactive', 1])

// 批量插入
def batchParams = [
    [name: '张三', email: 'zhangsan@example.com'],
    [name: '李四', email: 'lisi@example.com']
]

def result = handler.withBatch(10, '''
    INSERT INTO users (name, email) 
    VALUES (:name, :email)
''') { ps ->
    batchParams.each { params ->
        ps.addBatch(params)
    }
}
```

#### 4. 使用 SqlBuilder 构建查询

```groovy
// 获取 Wrapper 实例
def wrapper = handler.getWrapper()

// 创建 SqlBuilder
def sqlBuilder = wrapper.sqlBuilder()

// 添加查询字段
sqlBuilder.addSearchColumn("u.id", "u.name", "u.email", "d.department_name")

// 添加表名
sqlBuilder.addTableName("users u", "departments d")

// 设置为多表查询
sqlBuilder.isMultipleTable(true)

// 添加条件
sqlBuilder.conditionJoin("u.status", "active") { conditions, key, value ->
    return " AND ${wrapper.getColumn(key)} = :${key}"
}
.conditionJoin("u.created_at", "2023-01-01") { conditions, key, value ->
    return " AND ${wrapper.getColumn(key)} > :${key}"
}

// 添加其他 SQL 片段（如 JOIN、ORDER BY 等）
sqlBuilder.addOther("LEFT JOIN departments d ON u.department_id = d.id")
sqlBuilder.addOther("ORDER BY u.created_at DESC")
sqlBuilder.addOther("LIMIT 10 OFFSET 0")

// 添加条件参数值
sqlBuilder.addConditionVal("u.status", "active")
sqlBuilder.addConditionVal("u.created_at", "2023-01-01")

// 获取生成的 SQL 和参数
def sql = sqlBuilder.getSql()
def params = sqlBuilder.getConditionVal()

// 执行查询
def result = handler.rows(sql, params)
```

#### 5. 使用 SQL 模板

```groovy
def template = """
    SELECT * FROM users 
    WHERE 1=1
    #if(params.name)
      AND name LIKE :name
    #end
    #if(params.status)
      AND status = :status
    #end
    ORDER BY created_at DESC
    #if(params.pageSize && params.pageNo)
      LIMIT :pageSize OFFSET :offset
    #end
"""

def params = [
    name: "%张%",
    status: 'active',
    pageSize: 10,
    pageNo: 1
]

def result = sqlTemplateEngine.execute(template, params)
```

## 高级配置

### 本地连接池配置（HikariCP）

```groovy
def props = new DatasourceProperties(
    url: 'jdbc:postgresql://localhost:5432/mydb',
    username: 'user',
    password: 'password',
    driverClassName: 'org.postgresql.Driver'
)

// 本地连接池配置（HikariCP）
Map<String, Object> poolConfig = [
    // HikariCP 核心配置
    maxPoolSize: 20,              // 最大连接池大小
    minPoolSize: 5,               // 最小空闲连接数
    connectionTimeout: 30000,     // 连接超时时间(毫秒)
    idleTimeout: 600000,          // 空闲连接超时时间(毫秒)
    
    // 数据源特定属性（通过 addDataSourceProperty 设置）
    cachePrepStmts: "true",
    prepStmtCacheSize: "250",
    prepStmtCacheSqlLimit: "2048",
    useServerPrepStmts: "true"
]

// 创建本地连接池处理器
def localHandler = new PGSqlLocalPool(props, poolConfig)
```

### 分布式连接池配置（Aeron）

```groovy
// 分布式连接池配置（适用于 PGSqlPoolHandler 等）
Map<String, Object> poolConfig = [
    // 连接池大小
    initialSize: 5,                // 初始连接数
    maxActive: 20,                 // 最大活跃连接数
    minIdle: 5,                    // 最小空闲连接数
    maxIdle: 10,                   // 最大空闲连接数
    
    // 连接超时设置
    maxWait: 30000,                // 获取连接最大等待时间(毫秒)
    
    // 连接有效性检查
    validationQuery: 'SELECT 1',    // 验证查询
    testWhileIdle: true,           // 空闲时检查连接是否有效
    testOnBorrow: false,           // 获取连接时检查有效性
    testOnReturn: false,           // 归还连接时检查有效性
    
    // 连接回收策略
    timeBetweenEvictionRunsMillis: 60000,  // 检查空闲连接的间隔时间
    minEvictableIdleTimeMillis: 300000,    // 连接在池中最小生存时间
    
    // 其他配置
    removeAbandoned: true,         // 是否移除泄露的连接
    removeAbandonedTimeout: 1800,  // 泄露连接的超时时间(秒)
    logAbandoned: true,            // 是否记录泄露连接的日志
    
    // 连接属性
    connectionProperties: [
        'connectTimeout=30000',
        'socketTimeout=120000'
    ].join('&')
]

// 创建分布式连接池处理器
SqlPoolHandler poolHandler = new PGSqlPoolHandler(props, poolConfig)
```

## 最佳实践

1. **连接管理**
   - 使用连接池管理数据库连接
   - 合理配置连接池参数
   - 确保及时释放连接

2. **SQL 优化**
   - 使用预编译语句
   - 合理使用索引
   - 避免 SELECT *
   - 分页查询大数据集

3. **批量操作**
   - 使用批量插入/更新提高性能
   - 适当调整批量大小
   - 考虑使用 JDBC 批处理

## 常见问题

**Q: 如何处理数据库连接泄漏？**
A: 确保在使用完连接后正确关闭资源，可以使用 try-with-resources 或 withCloseable 语法。

**Q: 如何监控连接池状态？**
A: 可以通过 JMX 或实现自定义的监控接口来监控连接池状态。

## 贡献指南

欢迎提交 Issue 和 Pull Request。在提交代码前，请确保：
1. 代码符合编码规范
2. 添加必要的单元测试
3. 更新相关文档
4. 通过所有测试用例

## 许可证

[在此处添加许可证信息]

## 支持的数据库
- PostgreSQL
- MySQL
- Oracle
- SQL Server
- 更多...
