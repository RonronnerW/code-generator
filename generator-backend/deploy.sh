#!/bin/bash
## set -e

## 第一步：删除可能启动的老 generator-backend 容器
echo "开始删除 generator-backend 容器"
docker stop generator-backend || true
docker rm generator-backend || true
echo "完成删除 generator-backend 容器"

## 第二步：启动新的 generator-backend 容器 \
echo "开始启动 generator-backend 容器"
docker run -d \
--name generator-backend \
-e "SPRING_PROFILES_ACTIVE=prod" \
-v /docker_projects/generator-backend/log:/root/logs/ \
-p 28080:28080 \
generator-backend
echo "正在启动 generator-backend 容器中，需要等待 30 秒左右"