<div align=center><img src="/doc/image/logo.jpg"/></div>

<div align=center> Make BigData Export Easier!!! </div>

-------

# EasyFile
Make BigData Export Easier!!!

## 欢迎Star!!!

### 介绍

#### 什么是EasyFile

EasyFile-是为了提供更加便捷的文件服务,一整套Web大文件导出解决方案。可以轻松导出千万以上数据

#### 功能特性

支持（同步、异步）导出、文件压缩、流式导出、分页导出、导出缓存复用、多组分页导出、多组流式导出、多种异步触发机制 等特性。

优化缓解导出文件时对服务的内存和CPU影响。针对文件服务可做更多的管理。

提供给开发者更加通用、快捷、统一的实现的API方案；

### 解决问题

1、瞬时加载数据过大导致内存飙高不够平滑机器宕机风险很大

2、生成较大文件容易出现HTTP 超时，造成导出失败

3、相同条件的导出结果无法做到复用，需要继续生成导出文件资源浪费

4、导出任务集中出现没有可监控机制

5、开发者不仅需要关心数据查询逻辑同时需要关心文件生成逻辑

### 框架对比

与 Alibaba 的EasyExcel 相比,两者侧重点不同。

Alibaba EasyExcel 是一个Excel文件生成导出、导入 解析工具。

EasyFile 是一个大文件导出的解决方案。用于解决大文件导出时遇到的，文件复用，文件导出超时，内存溢出，瞬时CPU 内存飙高等等问题的一整套解决方案。 同时EasyFile 不仅可以用于Excel
文件的导出,也可以用于csv,pdf,word 等文件导出的管理（暂时需要用户自己集成基础导出下载类BaseDownloadExecutor 实现文件生成逻辑）。

而且,EasyFile和Alibaba EasyExcel 并不冲突，依然可以结合EasyExcel 使用,文件生成逻辑使用Alibaba EasyExcel 做自行拓展使用。

### 软件架构

EasyFile 提供两种模式

**Local模式(推荐)**:  需要提供本地的api 存储Mapper. 将数据存储到本地数据库中管理。

**Remote模式**：需要部署easyfile-server 服务，并设置客户端调用远程EasyFile 的域名。

### 代码结构

- easyfile-common: 公共模块服务

- easyfile-storage: 存储服务
- easyfile-storage-api: 存储服务API
- easyfile-storage-remote: 远程调用存储
- easyfile-storage-local: 本地数据源存储

- easyfile-spring-boot-starter: easyfile starter 包
- easyfile-server: easyfile 远程存储服务端

- easyfile-example: 样例工程
- easyfile-example-local: 本地储存样样例工程
- easyfile-example-remote: 远程存储样例工程

### 使用教程

#### 一、引入maven依赖

如果使用本地模式 引入maven

```xml
<dependency>
    <groupId>org.svnee</groupId>
    <artifactId>easyfile-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
   <groupId>org.svnee</groupId>
   <artifactId>easyfile-storage-local</artifactId>
   <version>1.0.0</version>
</dependency>
```

如果使用remote模式引入maven 依赖

```xml
<dependency>
   <groupId>org.svnee</groupId>
    <artifactId>easyfile-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
   <groupId>org.svnee</groupId>
   <artifactId>easyfile-storage-remote</artifactId>
   <version>1.0.0</version>
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

如果是使用Local模式，需要提供Client配置

```properties
##### easyfile-local-datasource
easyfile.local.datasource.type=com.zaxxer.hikari.HikariDataSource
easyfile.local.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
easyfile.local.datasource.url=jdbc:mysql://localhost:3306/test?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&serverTimezone=GMT%2B8
easyfile.local.datasource.username=root
easyfile.local.datasource.password=123456
```

需要执行SQL:

```sql
CREATE TABLE ef_async_download_task
(
    id                BIGINT (20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    task_code         VARCHAR(50) NOT NULL DEFAULT '' COMMENT '任务编码',
    task_desc         VARCHAR(50) NOT NULL DEFAULT '' COMMENT '任务描述',
    app_id            VARCHAR(50) NOT NULL DEFAULT '' COMMENT '归属系统 APP ID',
    unified_app_id    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '统一APP ID',
    enable_status     TINYINT (3) NOT NULL DEFAULT 0 COMMENT '启用状态',
    limiting_strategy VARCHAR(50) NOT NULL DEFAULT '' COMMENT '限流策略',
    version           INT (10) NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         VARCHAR(50) NOT NULL DEFAULT '' COMMENT '创建人',
    update_by         VARCHAR(50) NOT NULL DEFAULT '' COMMENT '更新人',
    is_deleted        BIGINT (20) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_app_id_task_code` (`task_code`,`app_id`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '异步下载任务';

CREATE TABLE ef_async_download_record
(
    id                    BIGINT (20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    download_task_id      BIGINT (20) NOT NULL DEFAULT 0 COMMENT '下载任务ID',
    app_id                VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'app ID',
    download_code         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载code',
    upload_status         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '上传状态',
    file_url              VARCHAR(512) NOT NULL DEFAULT '' COMMENT '文件路径',
    file_system           VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '文件所在系统',
    download_operate_by   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载操作人',
    download_operate_name VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载操作人',
    remark                VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '备注',
    notify_enable_status  TINYINT (3) NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '通知有效',
    max_server_retry      INT (3) NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry         INT (3) NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param         TEXT NULL COMMENT '重试执行参数',
    error_msg             VARCHAR(256) NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time     DATETIME NULL COMMENT '最新执行时间',
    invalid_time          DATETIME NULL COMMENT '链接失效时间',
    download_num          INT (3) NOT NULL DEFAULT 0 COMMENT '下载次数',
    version               INT (10) NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (id),
    KEY `idx_download_operate_by` (`download_operate_by`) USING BTREE,
    KEY `idx_operator_record` (`download_operate_by`,`app_id`,`create_time`),
    KEY `idx_upload_invalid` (`upload_status`,`invalid_time`,`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '异步下载记录';
```

如果是使用remote服务，需要部署easyfile-server 服务,Client提供配置

```properties
#### easyfile-storage-remote
easyfile.remote.username=example
easyfile.remote.password=example
easyfile.remote.server-addr=127.0.0.1:8080
easyfile.remote.namespace=remote-example
```

#### 四、异步文件处理器

[异步文件处理器配置](doc/AsyncFileHandler.md)

#### 五、实现下载器

[下载器](doc/BaseDownloadExecutror.md)

#### 六、easyfile-server 部署

如果使用**Remote模式**时,需要部署easyfile-server服务;

否则不需要进行部署

1、执行存储DB SQL \
2、部署服务




