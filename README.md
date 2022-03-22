# EasyFile

### 介绍

EasyFile-是提供更加便捷的文件导出服务

支持同步导出、异步导出、文件压缩、流式导出、分页导出等特性。

优化缓解导出文件时对服务的内存和CPU影响。针对文件服务可做更多的管理。

提供给开发者更加通用、快捷、统一的实现的API方案；

### 软件架构

EasyFile 提供两种模式

local 模式:  需要提供本地的api 存储Mapper. 将数据存储到本地数据库中管理。

remote模式：需要部署easyfile-server 服务，并设置客户端调用远程EasyFile 的域名。

### 使用教程

#### 一、引入maven依赖

如果使用本地模式 引入maven

```xml

<dependency>
    <groupId>org.svnee</groupId>
    <artifactId>easyfile-spring-boot-starter</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
<dependency>
    <groupId>org.svnee</groupId>
    <artifactId>easyfile-local-storage</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```

如果使用远程模式引入maven 依赖

```xml

<dependency>
    <groupId>org.svnee</groupId>
    <artifactId>easyfile-spring-boot-starter</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
<dependency>
    <groupId>org.svnee</groupId>
    <artifactId>easyfile-remote-storage</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```

#### 二、Client端需要提供文件上传服务进行实现接口

```java
package org.svnee.easyfile.storage.file;

import java.io.File;
import org.svnee.easyfile.common.bean.Pair;

/**
 * 文件上传服务
 *
 * @author svnee
 */
public interface UploadService {

    /**
     * 文件上传
     * 如果需要重试则需要抛出 org.svnee.easyfile.starter.exception.GenerateFileException
     *
     * @param file 文件
     * @param fileName 自定义生成的文件名
     * @param appId 服务ID
     * @return key: 文件系统 --> value:返回文件URL/KEY标识符
     */
    Pair<String, String> upload(File file, String fileName, String appId);

}
```

将文件上传到自己的文件存储服务

#### 三、额外处理

如果是使用Local模式，需要提供存储Mapper

`org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper`

`org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper`

需要执行SQL

如果是使用remote服务，需要部署easyfile-server 服务，并设置域名。配置到客户端

#### 四、客户端配置

如果使用默认异步文件处理器(`org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler`)

提供了一些配置

| 配置key                                                      | 描述                                | 默认值 |
| ------------------------------------------------------------ | ----------------------------------- | ------ |
| easyfile.default.async.download.handler.thread-pool.corePoolSize | 默认下载线程池核心线程数            | 10     |
| easyfile.default.async.download.handler.thread-pool.maximumPoolSize | 默认下载线程池最大线程池数          | 20     |
| easyfile.default.async.download.handler.thread-pool.keepAliveTime | 默认下载线程池最大空闲时间 单位：秒 | 30     |
| easyfile.default.async.download.handler.thread-pool.maxBlockingQueueSize | 默认下载线程池阻塞队列最大长度      | 2048   |

Client 配置

| 配置key                                   | 描述                                                         | 默认值            |
| ----------------------------------------- | ------------------------------------------------------------ | ----------------- |
| easyfile.download.enabled                 | EasyFile是否启用                                             | true              |
| easyfile.download.appId                   | Client端 AppId                                               |                   |
| easyfile.download.unifiedAppId            | Client端统一AppId                                            |                   |
| easyfile.download.localFileTempPath       | Client端下载文件本地临时目录                                 | /tmp              |
| easyfile.download.enableAutoRegister      | Client端自动注册下载任务开关                                 | false             |
| easyfile.download.enableCompressFile      | Client 是否开启文件压缩优化                                  | false             |
| easyfile.download.minEnableCompressMbSize | Client 启用文件压缩最小的大小，单位:MB 在启用文件压缩后生效 | 1                 |
| easyfile.download.exportAdvisorOrder      | Client下载切面顺序                                           | Integer.MAX_VALUE |

