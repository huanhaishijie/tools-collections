#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME="$DIR/linux-amd-jre"  # 内置 JDK/JRE
JAR="$DIR/connector-pool-server-1.0.0.jar"

# 检查是否已经启动
EXIST=$(pgrep -f "$JAR")
if [ -n "$EXIST" ]; then
    echo "Service already running (PID: $EXIST)"
    exit 1
fi

# 启动服务（后台运行）
"$JAVA_HOME/bin/java" \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-exports java.base/sun.nio.ch=ALL-UNNAMED \
  -Xmx1g \
  -cp "$JAR" com.yuezm.project.connector.Application &
echo "Service started with PID $!"