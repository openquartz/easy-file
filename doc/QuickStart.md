### Tutorial

#### 1. Maven Dependency Integration

If using Local Mode, integrate the following Maven dependency:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyfile-spring-boot-starter-local</artifactId>
    <version>${latestVersion}</version>
</dependency>
```

If using Remote Mode, integrate the following Maven dependency:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyfile-spring-boot-starter-remote</artifactId>
    <version>${latestVersion}</version>
</dependency>
```

#### 2. Client-Side File Upload Service Implementation

The client needs to implement the file upload service interface:

```java
package com.openquartz.easyfile.storage.file;

import java.io.File;
import com.openquartz.easyfile.common.bean.Pair;

/**
 * File Upload Service
 *
 * @author svnee
 */
public interface UploadService {

    /**
     * Uploads a file.
     * If retry is needed, throw com.openquartz.easyfile.core.exception.GenerateFileException
     *
     * @param file The file to upload
     * @param fileName Custom generated filename
     * @param appId Application ID
     * @return key: file system --> value: URL or key identifier for the uploaded file
     */
    Pair<String, String> upload(File file, String fileName, String appId);

}
```

Upload files to your own file storage service.

#### 3. Spring Boot Entry Point Configuration

Add the annotation scanning `com.openquartz.easyfile.starter.annotation.EnableEasyFileAutoConfiguration` at the service startup entry.

Example:

```java
/**
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyFileAutoConfiguration
@MapperScan("com.openquartz.easyfile.example.mapper")
public class LocalExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalExampleApplication.class);
    }
}
```

#### 4. Additional Setup

For Local mode, provide the client-side configuration:

```properties
##### easyfile-local-datasource
easyfile.local.datasource.type=com.zaxxer.hikari.HikariDataSource
easyfile.local.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
easyfile.local.datasource.url=jdbc:mysql://localhost:3306/test?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&serverTimezone=GMT%2B8
easyfile.local.datasource.username=root
easyfile.local.datasource.password=123456
```

Execute the following SQL statements:

```sql
CREATE TABLE ef_async_file_task
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
    deleted        BIGINT (20) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_app_id_task_code` (`task_code`,`app_id`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '异步下载任务';

CREATE TABLE ef_async_file_record
(
    id                    BIGINT (20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    task_id      BIGINT (20) NOT NULL DEFAULT 0 COMMENT '下载任务ID',
    app_id                VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'app ID',
    executor_code         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载code',
    handle_status         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '上传状态',
    file_url              VARCHAR(512) NOT NULL DEFAULT '' COMMENT '文件路径',
    file_system           VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '文件所在系统',
    operate_by   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载操作人',
    operate_name VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '下载操作人',
    remark                VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '备注',
    notify_enable_status  TINYINT (3) NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '通知有效',
    max_server_retry      TINYINT(3) NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry         TINYINT(3) NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param         TEXT NULL COMMENT '重试执行参数',
    error_msg             VARCHAR(256) NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time     DATETIME NULL COMMENT '最新执行时间',
    invalid_time          DATETIME NULL COMMENT '链接失效时间',
    download_num          TINYINT(3) NOT NULL DEFAULT 0 COMMENT '下载次数',
    execute_process       TINYINT(3) NOT NULL DEFAULT 0 COMMENT '执行进度',
    version               INT (10) NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (id),
    KEY                   `idx_operate_by` (`operate_by`) USING BTREE,
    KEY                   `idx_operator_record` (`operate_by`,`app_id`,`create_time`),
    KEY                   `idx_upload_invalid` (`handle_status`,`invalid_time`,[id](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-example/easyfile-example-local/src/main/java/com/openquartz/easyfile/example/model/School.java#L11-L11)),
    KEY                   `idx_create_time` (`create_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '异步下载记录';
```

For Remote mode deployment, you need to deploy the `easyfile-server` service and provide the following client-side configuration:

```properties
#### easyfile-storage-remote
easyfile.remote.username=example
easyfile.remote.password=example
easyfile.remote.server-addr=127.0.0.1:8080
easyfile.remote.namespace=remote-example
```

#### 5. Asynchronous File Handler

[Asynchronous File Handler Configuration](./AsyncFileHandler.md)

#### 6. Implementing Downloaders

[Downloader Documentation](BaseDownloadExecutor.md)

#### 7. Admin - Management Interface

EasyFile provides a simple Admin management interface (available since v1.2.0). To enable it, add the following Maven dependency:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyfile-ui</artifactId>
    <version>1.2.0</version>
</dependency>
```

The monitoring path for the service is: ip+port/easyfile-ui/. For example: `localhost:8080/easyfile-ui/`. The default admin credentials are: `admin / admin`.

You can change these via configuration:

```properties
easyfile.ui.admin.username=admin
easyfile.ui.admin.password=admin
```

**EasyFile UI Management Interface**

![EasyFileUI](./image/EasyfileUi.png)

#### 8. Deployment of easyfile-server

When using **Remote Mode**, the `easyfile-server` service must be deployed.

No deployment is required otherwise.

1. Execute the database SQL scripts \
2. Deploy the service

#### 9. Export Internationalization Support

[Internationalization Feature Support](./I18n.md)