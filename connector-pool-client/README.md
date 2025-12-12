# Connector Pool Client

## 项目概述
- 基于 Aeron 的高性能数据库连接池客户端
- 支持异步消息通信
- 使用 Protocol Buffers 进行数据序列化

## 功能特性
- 数据库连接池管理
- 异步消息通信
- 支持多种数据库类型
- 可配置的连接池参数

## 快速开始

### 环境要求
- JDK 1.8 或更高版本
- Gradle 构建工具
- Aeron 消息系统

### 安装
```bash
git clone [项目仓库地址]
cd connector-pool-client
gradle build
```

### 基本使用
1. 初始化客户端
2. 配置数据源
3. 发送请求
4. 处理响应

## 配置说明
### 数据源配置
- URL: 数据库连接地址
- 用户名/密码
- 连接池参数
  - 最大连接数
  - 最小连接数
  - 空闲超时
  - 连接超时

## API 参考
### 核心类
- `Client`: 主客户端类
- `Chat`: 消息处理类
- `DataSourceInfo`: 数据源信息类

### 主要方法
- `Client.getInstance()`: 获取客户端实例
- `send()`: 发送请求
- 事件监听器

## 示例代码
```groovy
// 示例代码展示如何初始化和使用客户端
def client = Client.getInstance("127.0.0.1", 38881, 2500, "127.0.0.1")
// ... 配置数据源
client.send(dataSourceInfo) { response ->
    // 处理响应
}
```

## 协议说明
### 消息格式
- RequestInfo
- ExecInfo
- Response
- DataSourceInfo

## 开发指南
### 构建项目
```bash
gradle build
```

### 运行测试
```bash
gradle test
```

## 贡献指南
1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 创建 Pull Request

## 许可证
[许可证类型]

## 联系方式
- 项目维护者
- 问题跟踪
