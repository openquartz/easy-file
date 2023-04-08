####基础镜像
FROM eclipse-temurin:8-jre

### author
MAINTAINER svnee

### 定义参数
ARG JAR_FILE

### 创建并进入工作目录
RUN mkdir -p /easyfile-server
WORKDIR /easyfile-server

## maven 插件构建时得到buildArgs中的值
COPY ${JAR_FILE} easyfile-server.jar

### 设置时区,可以通过docker run -e "JAVA_OPTS=" 进行覆盖
ENV TZ = Asia/Shanghai JAVA_OPTS = "-Xms256m -Xmx256m"

### 暴露端口
EXPOSE 9900

#### 容器启动命令
CMD java ${JAVA_OPTS} -java easyfile-server.jar