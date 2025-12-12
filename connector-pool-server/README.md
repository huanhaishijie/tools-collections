# 数据库连接池服务器 (Database Connection Pool Server)

## 项目概述

数据库连接池服务器是一个高性能的数据库连接管理服务，基于 Aeron 框架实现，支持通过 UDP 协议进行数据库连接管理和 SQL 执行。

## 功能特性

- 🚀 高性能：基于 Aeron 框架实现，提供低延迟、高吞吐量的数据库连接管理
- 🔌 多数据库支持：支持多种关系型数据库（通过 JDBC 驱动）
- 🔄 连接池管理：使用 HikariCP 实现高效的数据库连接池
- 📡 网络通信：基于 UDP 协议，支持请求/响应模式
- 🛠️ 动态配置：支持运行时动态添加/移除数据库连接
- 🔍 SQL 执行：支持执行查询和更新操作
- 📊 结果集处理：支持将查询结果转换为 JSON 格式

## 快速开始

### 环境要求

- Java 17 或更高版本
- Gradle 8 或更高版本
- 目标数据库的 JDBC 驱动

### 构建项目

```bash
gradle clean build
```

### 运行服务

```bash
java -jar build/libs/connector-pool-server-<version>.jar
```

默认监听端口：`38880`

## API 文档

### 1. 注册数据库连接

**请求参数 (DataSourceInfo):**

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| url | string | 是 | 数据库连接URL |
| type | string | 是 | 数据库类型 (如: mysql, postgresql, oracle等) |
| username | string | 是 | 数据库用户名 |
| password | string | 是 | 数据库密码 |
| maxPoolSize | int | 否 | 连接池最大连接数 |
| minPoolSize | int | 否 | 连接池最小空闲连接数 |
| idleTimeout | int | 否 | 连接空闲超时时间(毫秒) |
| connectionTimeout | int | 否 | 连接超时时间(毫秒) |
| other | map<string, string> | 否 | 其他数据库连接属性 |

### 2. 执行SQL查询

**请求参数 (DataSourceInfo.other):**

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sql | string | 是 | 要执行的SQL查询语句 |
| params | JSON数组 | 否 | SQL参数，按顺序传入 |

### 3. 执行SQL更新

**请求参数 (DataSourceInfo.other):**

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sql | string | 是 | 要执行的SQL更新语句 |
| params | JSON数组 | 否 | SQL参数，按顺序传入 |

## 配置说明

### 服务器配置

可以通过修改 `Application.groovy` 中的以下参数来配置服务器：

```groovy
private static int port = 38880  // 监听端口
private static String serverUrl = "aeron:udp?endpoint=0.0.0.0:$port"  // 服务器URL
private static int streamId = 2500  // 流ID
```

### 数据库连接池配置

支持通过 `DataSourceInfo` 消息配置 HikariCP 连接池参数：

- `maxPoolSize`: 连接池最大连接数
- `minPoolSize`: 连接池最小空闲连接数
- `idleTimeout`: 连接空闲超时时间(毫秒)
- `connectionTimeout`: 连接超时时间(毫秒)
- 其他 HikariCP 参数可以通过 `other` 字段传入

## 开发指南

### 项目结构

```
src/
  main/
    groovy/
      com/yuezm/project/connector/
        Application.groovy    # 主应用类
        DBServer.groovy      # 数据库服务实现
    proto/
      dbgateway.proto        # Protocol Buffers 定义
```

### 构建与测试

```bash
# 编译项目
gradle compileGroovy

# 运行测试
gradle test

# 构建可执行JAR
gradle shadowJar
```

## 贡献指南

欢迎提交 Issue 和 Pull Request。

### 提交规范

- 提交信息请遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范
- 提交代码前请确保通过所有测试

## 许可证

[Apache License 2.0](LICENSE)

## 作者

yuezm