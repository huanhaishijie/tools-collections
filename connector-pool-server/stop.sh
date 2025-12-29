#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$DIR/connector-pool-server-1.0.0.jar"

# 查找运行的进程
PIDS=$(pgrep -f "$JAR")

if [ -z "$PIDS" ]; then
    echo "No running service found."
    exit 0
fi

for PID in $PIDS; do
    echo "Stopping service (PID: $PID) ..."
    kill $PID          # 正常停止
    sleep 2
    if kill -0 $PID 2>/dev/null; then
        echo "Force killing $PID ..."
        kill -9 $PID   # 强制停止
    fi
done

echo "All matching services stopped."
