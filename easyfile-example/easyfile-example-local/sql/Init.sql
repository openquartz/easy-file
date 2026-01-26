CREATE TABLE `test_school`
(
    `id`    int(10) NOT NULL,
    `name`  varchar(500) NOT NULL,
    `grade` varchar(255) DEFAULT '等级',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `test_student`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `name`      varchar(50)  NOT NULL,
    `age`       int(10) NOT NULL,
    `school`    varchar(128) NOT NULL,
    `address`   varchar(512) NOT NULL,
    `school_id` int(10) NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`),
    KEY         `idx_school` (`school_id`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000001 DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE ef_async_import_task
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
  DEFAULT CHARSET = utf8mb4 COMMENT '异步导入任务';

CREATE TABLE ef_async_import_record
(
    id                    BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    import_task_id        BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '导入任务ID',
    app_id                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT 'app ID',
    import_code           VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入code',
    upload_status         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '上传状态',
    file_url              VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '文件路径',
    file_name             VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '文件名',
    file_system           VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '文件所在系统',
    import_operate_by     VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入操作人',
    import_operate_name   VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入操作人',
    remark                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '备注',
    notify_enable_status  TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email          VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '通知有效',
    max_server_retry      INT(3)              NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry         INT(3)              NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param         TEXT                NULL COMMENT '重试执行参数',
    error_msg             VARCHAR(256)        NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time     DATETIME            NULL COMMENT '最新执行时间',
    invalid_time          DATETIME            NULL COMMENT '链接失效时间',
    execute_process       INT(3)              NOT NULL DEFAULT 0 COMMENT '执行进度',
    error_file_url        VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '失败文件路径',
    success_rows          INT(10)             NOT NULL DEFAULT 0 COMMENT '成功行数',
    fail_rows             INT(10)             NOT NULL DEFAULT 0 COMMENT '失败行数',
    total_rows            INT(10)             NOT NULL DEFAULT 0 COMMENT '总行数',
    version               INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    locale                VARCHAR(16)         NOT NULL default '' COMMENT '语言',
    PRIMARY KEY (id),
    KEY `idx_import_operate_by` (`import_operate_by`) USING BTREE,
    KEY `idx_operator_record` (`import_operate_by`, `app_id`, `create_time`),
    KEY `idx_upload_invalid` (`upload_status`, `invalid_time`, `id`)
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
    creating_owner    varchar(50) not null default '' comment '触发者',
    processing_owner  varchar(50) not null default '' comment '执行者',
    UNIQUE `ux_register_id` (register_id),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '触发执行';
