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

#### 多数据库支持
- PostgreSQL
- MySQL
- Oracle
- SQL Server
- 其他兼容 JDBC 的数据库

### 2. SQL 构建器 (SqlBuilder)

#### 查询构建
- 链式 API 设计
- 支持 SELECT 语句构建
- 条件组合 (AND/OR)
- 排序和分页支持

#### 条件构建
- 等值/不等值比较
- 模糊查询
- IN/NOT IN 条件
- BETWEEN 条件
- IS NULL/IS NOT NULL

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

### 4. 数据库连接管理

#### 连接池配置
- 初始连接数
- 最大连接数
- 连接超时设置
- 空闲连接回收
- 连接验证

#### 监控
- 连接池状态
- 执行统计
- 性能指标

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

- 相关数据库驱动

## 快速开始

## 快速开始

### 环境要求
- Java 8 或更高版本
- Groovy
- 相关数据库驱动（如 PostgreSQL、MySQL 等）

### 添加依赖

```gradle
dependencies {
    implementation 'com.yuezm:sql-toolkit:1.0.0'
    // 添加数据库驱动，例如 PostgreSQL
    implementation 'org.postgresql:postgresql:42.3.1'
}
```

### 基本使用

#### 1. 创建数据库连接

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
    二进制编码，相比 JSON / XML 通常 小 3～10 倍
    使用 Varint 对整数进行压缩
    不携带字段名，仅使用 field number
    2.生成代码，无反射（或极少反射）生成代码，无反射（或极少反射）
    结构化内存布局，CPU Cache 友好
    在高并发、低延迟场景（RPC、消息系统）中：
    明显优于 JSON、XML
    通常略慢于极致手写二进制协议，但可维护性远高
    3.明确的 Schema（IDL）
    通过 .proto 文件： 明确字段类型
    明确字段编号（tag）
    明确必填 / 可选 / repeated
    这带来：
    编译期校验
    消除“弱约束 JSON”的隐性 Bug
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

#### 2. 执行查询

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

#### 3. 使用 SqlBuilder 构建查询

```groovy
def query = new SqlBuilder()
    .select('u.id', 'u.name', 'u.email', 'd.department_name')
    .from('users u')
    .leftJoin('departments d').on('u.department_id = d.id')
    .where()
    .eq('u.status', 'active')
    .and()
    .gt('u.created_at', '2023-01-01')
    .orderBy('u.created_at', 'DESC')
    .limit(10)
    .offset(0)
    .build()

def result = handler.rows(query.sql, query.conditionVal)
```

#### 4. 使用 SQL 模板

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

### 高级配置

#### 连接池配置

```groovy
def props = new DatasourceProperties(
    url: 'jdbc:postgresql://localhost:5432/mydb',
    username: 'user',
    password: 'password',
    driverClassName: 'org.postgresql.Driver'
)

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

// 创建带连接池的处理器
SqlPoolHandler poolHandler = new PGSqlPoolHandler(props, poolConfig)
```

### 最佳实践

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

### 常见问题

**Q: 如何处理数据库连接泄漏？**
A: 确保在使用完连接后正确关闭资源，可以使用 try-with-resources 或 withCloseable 语法。

**Q: 如何监控连接池状态？**
A: 可以通过 JMX 或实现自定义的监控接口来监控连接池状态。

### 贡献指南

欢迎提交 Issue 和 Pull Request。在提交代码前，请确保：
1. 代码符合编码规范
2. 添加必要的单元测试
3. 更新相关文档
4. 通过所有测试用例

### 许可证

[在此处添加许可证信息]

### 基础用法

#### 1. 添加 Maven 依赖
```xml
<dependency>
    <groupId>com.yuezm</groupId>
    <artifactId>sql-toolkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2. 创建数据库连接
```groovy
// 基本连接
import com.yuezm.project.sql.*

def props = new DatasourceProperties(
    url: 'jdbc:postgresql://localhost:5432/mydb',
    username: 'user',
    password: 'password',
    driverClassName: 'org.postgresql.Driver'
)

// 创建连接池
SqlPoolHandler handler = SqlHandler.buildSql('postgresql', props)

// 或者使用连接池配置
Map poolConfig = [
    initialSize: 5,
    maxActive: 20,
    minIdle: 5,
    maxWait: 60000,
    timeBetweenEvictionRunsMillis: 60000,
    minEvictableIdleTimeMillis: 300000,
    validationQuery: 'SELECT 1',
    testWhileIdle: true,
    testOnBorrow: false,
    testOnReturn: false
]

SqlPoolHandler poolHandler = new PGSqlPoolHandler(props, poolConfig)
```

#### 3. 基本 CRUD 操作

**查询示例**
```groovy
// 查询单条记录
def user = handler.firstRow('SELECT * FROM users WHERE id = ?', [1])

// 查询多条记录
def activeUsers = handler.rows('SELECT * FROM users WHERE status = ?', ['active'])

// 使用 SqlBuilder 构建复杂查询
def query = new SqlBuilder()
    .select('u.id', 'u.name', 'u.email', 'd.department_name')
    .from('users u')
    .leftJoin('departments d').on('u.department_id = d.id')
    .where()
    .eq('u.status', 'active')
    .and()
    .gt('u.created_at', '2023-01-01')
    .orderBy('u.created_at', 'DESC')
    .limit(10)
    .offset(0)
    .build()

def result = handler.rows(query.sql, query.conditionVal)
```

**插入/更新/删除示例**
```groovy
// 插入数据
def userId = handler.executeInsert('''
    INSERT INTO users (name, email, status) 
    VALUES (:name, :email, :status)
''', [name: '张三', email: 'zhangsan@example.com', status: 'active'])

// 批量插入
def batchParams = [
    [name: '李四', email: 'lisi@example.com', status: 'active'],
    [name: '王五', email: 'wangwu@example.com', status: 'inactive']
]

def result = handler.withBatch(10, '''
    INSERT INTO users (name, email, status) 
    VALUES (:name, :email, :status)
''') { ps ->
    batchParams.each { params ->
        ps.addBatch(params)
    }
}

// 更新数据
def updated = handler.executeUpdate('''
    UPDATE users 
    SET status = :newStatus 
    WHERE id = :id
''', [id: 1, newStatus: 'inactive'])

// 删除数据
def deleted = handler.execute('''
    DELETE FROM users 
    WHERE status = :status
''', [status: 'inactive'])
```

#### 4. 使用 SQL 模板

**基本模板**
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
    #if(params.startDate && params.endDate)
      AND created_at BETWEEN :startDate AND :endDate
    #end
    ORDER BY created_at DESC
    #if(params.pageSize && params.pageNo)
      LIMIT :pageSize OFFSET :offset
    #end
"""

def params = [
    name: "%张%",
    status: 'active',
    startDate: '2023-01-01',
    endDate: '2023-12-31',
    pageSize: 10,
    pageNo: 1
]

def result = sqlTemplateEngine.execute(template, params)
```

**带循环的模板**
```groovy
def template = """
    SELECT * FROM users 
    WHERE id IN (
        #each ids as id
            #if(id_has_next)
                :ids_${id_index},
            #else
                :ids_${id_index}
            #end
        #end
    )
"""

def params = [
    ids: [1, 2, 3, 4, 5]  // 自动展开为 ids_0=1, ids_1=2, ...
]

def result = sqlTemplateEngine.execute(template, params)
```


### 高级配置

#### 连接池配置

```groovy
def props = new DatasourceProperties(
    url: 'jdbc:postgresql://localhost:5432/mydb',
    username: 'user',
    password: 'password',
    driverClassName: 'org.postgresql.Driver'
)

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

// 创建带连接池的处理器
SqlPoolHandler poolHandler = new PGSqlPoolHandler(props, poolConfig)
```

### 性能优化建议

1. **连接池调优**
   - 根据应用负载调整连接池大小
   - 设置合理的超时时间
   - 启用空闲连接回收

2. **SQL 优化**
   - 使用预编译语句
   - 合理使用索引
   - 避免 SELECT *
   - 分页查询大数据集

3. **批量操作**
   - 使用批量插入/更新
   - 适当调整批量大小
   - 考虑使用 JDBC 批处理

4. **缓存策略**
   - 实现查询结果缓存
   - 使用二级缓存
   - 合理设置缓存过期时间

### 常见问题

**Q: 如何处理数据库连接泄漏？**
A: 确保在使用完连接后正确关闭资源，可以使用 try-with-resources 或 withCloseable 语法。

**Q: 如何监控连接池状态？**
A: 可以通过 JMX 或实现自定义的监控接口来监控连接池状态。


### 贡献指南

欢迎提交 Issue 和 Pull Request。在提交代码前，请确保：
1. 代码符合编码规范
2. 添加必要的单元测试
3. 更新相关文档
4. 通过所有测试用例

### 许可证

[在此处添加许可证信息]

## 支持的数据库
- PostgreSQL
- MySQL
- Oracle
- SQL Server
- 更多...

## 许可证
[请指定许可证]

## 贡献指南
[贡献指南]