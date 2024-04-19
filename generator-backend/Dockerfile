# 使用包含已经构建好的 JAR 文件的基础镜像
FROM openjdk:17-oracle

# 设置工作目录
WORKDIR ./

# 复制已构建好的 JAR 文件到镜像中
COPY ./generator-backend-0.0.1-SNAPSHOT.jar app.jar

# 设置环境变量
ENV PARAMS=""
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 容器启动命令
ENTRYPOINT ["sh", "-c", "java -jar ./app.jar $PARAMS --spring.profiles.active=prod"]