-- EasyFile 导入功能数据库脚本
-- 对齐异步导出三表结构: task / record / trigger
-- ImportStatusEnum 存储码: none/executing/success/partial_success/fail/revoke/invalid

-- 异步导入任务定义表（对齐 ef_async_download_task）
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

-- 异步导入记录表（对齐 ef_async_download_record）
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

-- 导入触发执行表（对齐 ef_async_download_trigger）
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

-- 导入错误详情表（可选，管理端明细；主路径可用结果文件覆盖）
CREATE TABLE ef_import_error_detail
(
    id               BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    register_id      BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '导入记录ID',
    row_number       INT(10)             NOT NULL DEFAULT 0 COMMENT '行号',
    row_key          VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '行键值',
    column_name      VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '列名',
    column_index     INT(5)              NOT NULL DEFAULT 0 COMMENT '列索引',
    error_value      TEXT                NULL COMMENT '错误值',
    error_type       VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '错误类型: VALIDATION-验证错误, BUSINESS-业务错误',
    error_message    VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '错误信息',
    suggested_action VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '建议操作',
    create_time      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY `idx_register_id` (`register_id`) USING BTREE,
    KEY `idx_error_type` (`error_type`) USING BTREE,
    KEY `idx_row_number` (`row_number`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '导入错误详情表';

-- 数据字典：导入状态（dict_code 与 ImportStatusEnum.getCode() 一致）
INSERT INTO `sys_dict_type` (`dict_type`, `dict_type_name`, `dict_type_desc`, `create_time`, `update_time`)
VALUES ('import_status', '导入状态', '导入任务执行状态', NOW(), NOW()) ON DUPLICATE KEY UPDATE dict_type_name = '导入状态';

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_desc`, `sort`, `status`, `create_time`, `update_time`) VALUES
('import_status', 'none', '未开始', '导入任务未开始执行', 1, 1, NOW(), NOW()),
('import_status', 'executing', '执行中', '导入任务正在执行', 2, 1, NOW(), NOW()),
('import_status', 'success', '成功', '全部行处理成功', 3, 1, NOW(), NOW()),
('import_status', 'partial_success', '部分成功', '存在行级失败并产出结果文件', 4, 1, NOW(), NOW()),
('import_status', 'fail', '失败', '任务级失败', 5, 1, NOW(), NOW()),
('import_status', 'revoke', '撤销', '导入任务已取消/撤销', 6, 1, NOW(), NOW()),
('import_status', 'invalid', '失效', '源文件或任务已失效', 7, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), dict_desc = VALUES(dict_desc);

-- 数据字典：错误类型
INSERT INTO `sys_dict_type` (`dict_type`, `dict_type_name`, `dict_type_desc`, `create_time`, `update_time`)
VALUES ('error_type', '错误类型', '导入错误类型分类', NOW(), NOW()) ON DUPLICATE KEY UPDATE dict_type_name = '错误类型';

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_desc`, `sort`, `status`, `create_time`, `update_time`) VALUES
('error_type', 'VALIDATION', '验证错误', '数据验证失败', 1, 1, NOW(), NOW()),
('error_type', 'BUSINESS', '业务错误', '业务处理失败', 2, 1, NOW(), NOW()),
('error_type', 'SYSTEM', '系统错误', '系统异常导致的错误', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), dict_desc = VALUES(dict_desc);
