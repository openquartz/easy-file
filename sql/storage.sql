CREATE TABLE ef_async_download_task
(
    id                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    task_code         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '任务编码',
    task_desc         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '任务描述',
    app_id            VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '归属系统 APP ID',
    unified_app_id    VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '统一APP ID',
    enable_status     TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '启用状态',
    limiting_strategy VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '限流策略',
    version           INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    is_deleted        BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_app_id_task_code` (`task_code`, `app_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '异步下载任务';

CREATE TABLE ef_async_download_record
(
    id                    BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    download_task_id      BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '下载任务ID',
    app_id                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT 'app ID',
    download_code         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '下载code',
    upload_status         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '上传状态',
    file_url              VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '文件路径',
    file_name             VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '文件名',
    file_system           VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '文件所在系统',
    download_operate_by   VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '下载操作人',
    download_operate_name VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '下载操作人',
    remark                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '备注',
    notify_enable_status  TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email          VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '通知有效',
    max_server_retry      INT(3)              NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry         INT(3)              NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param         TEXT                NULL COMMENT '重试执行参数',
    error_msg             VARCHAR(256)        NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time     DATETIME            NULL COMMENT '最新执行时间',
    invalid_time          DATETIME            NULL COMMENT '链接失效时间',
    download_num          INT(3)              NOT NULL DEFAULT 0 COMMENT '下载次数',
    execute_process       INT(3)              NOT NULL DEFAULT 0 COMMENT '执行进度',
    version               INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    locale                VARCHAR(16)         NOT NULL default '' COMMENT '语言',
    PRIMARY KEY (id),
    KEY `idx_download_operate_by` (`download_operate_by`) USING BTREE,
    KEY `idx_operator_record` (`download_operate_by`, `app_id`, `create_time`),
    KEY `idx_upload_invalid` (`upload_status`, `invalid_time`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '异步下载记录';

CREATE TABLE ef_async_download_trigger
(
    id                BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'Id',
    register_id       BIGINT(20)  NOT NULL DEFAULT -1 COMMENT '注册ID',
    trigger_status    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '触发状态',
    start_time        DATETIME    NOT NULL COMMENT '开始时间',
    last_execute_time DATETIME    NOT NULL COMMENT '最新执行时间',
    trigger_count     INT(3)      NOT NULL DEFAULT 0 COMMENT '触发次数',
    creating_owner    varchar(50) not null default '' comment '触发者',
    processing_owner  varchar(50) not null default '' comment '执行者',
    UNIQUE `ux_register_id` (register_id),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '触发执行';
-- ---------------------------------------------------------------------------
-- Async import (aligned with export task/record/trigger; see also sql/import_tables.sql)
-- ImportStatusEnum store codes: none/executing/success/partial_success/fail/revoke/invalid
-- ---------------------------------------------------------------------------

CREATE TABLE ef_async_import_task
(
    id             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    task_code      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入任务编码',
    task_desc      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '任务描述',
    app_id         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '归属系统 APP ID',
    unified_app_id VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '统一 APP ID',
    enable_status  TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '启用状态',
    version        INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    is_deleted     BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_app_id_task_code` (`task_code`, `app_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '异步导入任务';

CREATE TABLE ef_async_import_record
(
    id                    BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    import_task_id        BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '导入任务ID',
    app_id                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT 'app ID',
    import_code           VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入code',
    import_status         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入状态: NONE/EXECUTING/SUCCESS/PARTIAL_SUCCESS/FAIL/CANCEL/INVALID (store: none/executing/success/partial_success/fail/revoke/invalid)',
    source_file_url       VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '源文件路径',
    source_file_name      VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '源文件名',
    file_type             VARCHAR(20)         NOT NULL DEFAULT '' COMMENT '文件类型: xlsx, xls, csv',
    file_hash             VARCHAR(64)         NOT NULL DEFAULT '' COMMENT '文件哈希',
    business_unique_key   VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '业务幂等键',
    result_file_url       VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '结果文件路径',
    total_count           BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '总行数',
    success_count         BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '成功行数',
    fail_count            BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '失败行数',
    execute_process       INT(3)              NOT NULL DEFAULT 0 COMMENT '执行进度',
    resume_position       BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '断点续传位置',
    notify_enable_status  TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email          VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '通知邮箱',
    max_server_retry      INT(3)              NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry         INT(3)              NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param         TEXT                NULL COMMENT '导入执行参数',
    error_msg             VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time     DATETIME            NULL COMMENT '最新执行时间',
    version               INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    locale                VARCHAR(16)         NOT NULL DEFAULT '' COMMENT '语言',
    PRIMARY KEY (id),
    KEY `idx_import_operate_by` (`create_by`) USING BTREE,
    KEY `idx_operator_record` (`create_by`, `app_id`, `create_time`),
    KEY `idx_import_status` (`import_status`, `id`),
    KEY `idx_business_unique_key` (`business_unique_key`),
    KEY `idx_app_import_code` (`app_id`, `import_code`) USING BTREE,
    KEY `idx_file_hash` (`file_hash`) USING BTREE,
    KEY `idx_import_task_id` (`import_task_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '异步导入记录';

CREATE TABLE ef_async_import_trigger
(
    id                BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'Id',
    register_id       BIGINT(20)  NOT NULL DEFAULT -1 COMMENT '注册ID',
    trigger_status    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '触发状态',
    start_time        DATETIME    NOT NULL COMMENT '开始时间',
    last_execute_time DATETIME    NOT NULL COMMENT '最新执行时间',
    trigger_count     INT(3)      NOT NULL DEFAULT 0 COMMENT '触发次数',
    creating_owner    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '触发者',
    processing_owner  VARCHAR(50) NOT NULL DEFAULT '' COMMENT '执行者',
    UNIQUE KEY `ux_register_id` (`register_id`),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '导入触发执行';
