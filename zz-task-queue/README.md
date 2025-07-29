# 任务队列系统 (Task Queue System)

## 项目依赖

```xml
<dependency>
    <groupId>com.yzm.project</groupId>
    <artifactId>task-queue</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy</artifactId>
    <version>3.0.10</version>
</dependency>
```

## 项目简介

这是一个简单易用的任务队列系统，基于Spring Boot和Groovy开发，用于替代Spring事件机制的复杂性。

## 设计背景

Spring的事件机制虽然功能强大，但存在以下问题：
- 需要定义接口
- 需要在配置文件中配置事件监听器
- 使用起来相对复杂

本项目提供了一个更简单、更高效的任务队列解决方案，特别适合生产环境使用。

## 使用说明

### 1. 任务对象定义

```java
@Data
public class FileTaskDto implements Serializable {
    private String id;
    private String fileId;
    private String datasourceId;
}
```

### 2. 消费者实现

```java
public class FileConsumer extends Consumer<FileTaskDto> {
    @Override
    public TaskState consume(FileTaskDto fileTaskDto) {
        // 业务逻辑
        return TaskState.SUCCESS;
    }
}
```

### 3. 任务生产

```java
// 生产任务
Producer.getInstance().send("任务唯一ID", new FileTaskDto());
```

## 注意事项

- 请确保数据库文件有足够的读写权限
- 建议在生产环境中使用更稳定的数据库系统
- 任务队列的容量和性能需要根据实际需求调整

## 联系方式

如需技术支持或有任何问题，请联系：
- 项目维护者：yuezm
- 邮箱：待补充

## 版权信息

版权所有 © 2025 项目所有者

## 版本历史

- 1.0.0 - 初始版本
- 1.0.1 - 1.修复任务null赋值失败导致无法消费 2.优化sql展示

## 致谢

感谢所有贡献者和使用本项目的开发者