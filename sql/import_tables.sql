-- EasyFile 导入功能数据库脚本
-- 创建时间: 2026-05-23

-- 导入记录表
CREATE TABLE ef_async_import_record
(
    id                    BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    app_id                VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '应用ID',
    import_code           VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入编码',
    import_status         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入状态: NONE-未开始, EXECUTING-执行中, COMPLETED-已完成, FAILED-失败, CANCELLED-已取消',
    oss_url               VARCHAR(512)        NOT NULL DEFAULT '' COMMENT 'OSS文件URL',
    file_name             VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '文件名',
    file_type             VARCHAR(20)         NOT NULL DEFAULT '' COMMENT '文件类型: xlsx, xls, csv',
    file_hash             VARCHAR(64)         NOT NULL DEFAULT '' COMMENT '文件MD5哈希值(用于幂等性检查)',
    business_unique_key   VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '业务唯一键(用于幂等性检查)',
    execute_param         TEXT                NULL COMMENT '执行参数(JSON格式)',
    total_count           BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '总记录数',
    success_count         BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '成功记录数',
    fail_count            BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '失败记录数',
    resume_position       BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '断点续传位置',
    result_file_url       VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '结果文件URL',
    error_msg             VARCHAR(1000)       NOT NULL DEFAULT '' COMMENT '错误信息',
    max_retry             INT(3)              NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    current_retry         INT(3)              NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_process       INT(3)              NOT NULL DEFAULT 0 COMMENT '执行进度(0-100)',
    enable_async          TINYINT(1)          NOT NULL DEFAULT 1 COMMENT '是否异步执行',
    support_resumable     TINYINT(1)          NOT NULL DEFAULT 1 COMMENT '是否支持断点续传',
    page_size             INT(5)              NOT NULL DEFAULT 1000 COMMENT '分页大小',
    last_execute_time     DATETIME            NULL COMMENT '最新执行时间',
    version               INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by             VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_business_key` (`app_id`, `import_code`, `business_unique_key`) USING BTREE,
    KEY `idx_app_import_code` (`app_id`, `import_code`) USING BTREE,
    KEY `idx_import_status` (`import_status`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE,
    KEY `idx_file_hash` (`file_hash`) USING BTREE,
    KEY `idx_business_unique` (`business_unique_key`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '异步导入记录表';

-- 导入错误详情表
CREATE TABLE ef_import_error_detail
(
    id                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    register_id       BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '导入记录ID',
    row_number        INT(10)             NOT NULL DEFAULT 0 COMMENT '行号',
    row_key           VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '行键值',
    column_name       VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '列名',
    column_index      INT(5)              NOT NULL DEFAULT 0 COMMENT '列索引',
    error_value       TEXT                NULL COMMENT '错误值',
    error_type        VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '错误类型: VALIDATION-验证错误, BUSINESS-业务错误',
    error_message     VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '错误信息',
    suggested_action  VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '建议操作',
    create_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY `idx_register_id` (`register_id`) USING BTREE,
    KEY `idx_error_type` (`error_type`) USING BTREE,
    KEY `idx_row_number` (`row_number`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '导入错误详情表';

-- 导入模板表
CREATE TABLE ef_import_template
(
    id                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    app_id            VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '应用ID',
    template_code     VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '模板编码',
    template_name     VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '模板名称',
    template_desc     VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '模板描述',
    file_type         VARCHAR(20)         NOT NULL DEFAULT '' COMMENT '文件类型: xlsx, xls, csv',
    column_config     TEXT                NULL COMMENT '列配置(JSON格式)',
    validation_rules  TEXT                NULL COMMENT '验证规则(JSON格式)',
    sample_file_url   VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '示例文件URL',
    version           INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    is_deleted        TINYINT(1)          NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_template_code` (`app_id`, `template_code`) USING BTREE,
    KEY `idx_template_status` (`is_deleted`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '导入模板表';

-- 导入任务执行器表
CREATE TABLE ef_import_executor
(
    id                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    app_id            VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '应用ID',
    import_code       VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入编码',
    executor_name     VARCHAR(100)        NOT NULL DEFAULT '' COMMENT '执行器名称',
    executor_class    VARCHAR(200)        NOT NULL DEFAULT '' COMMENT '执行器类名',
    executor_desc     VARCHAR(500)        NOT NULL DEFAULT '' COMMENT '执行器描述',
    enable_status     TINYINT(1)          NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用, 1-启用',
    version           INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY `uniq_import_code` (`app_id`, `import_code`) USING BTREE,
    KEY `idx_executor_status` (`enable_status`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '导入任务执行器表';

-- 数据字典：导入状态
INSERT INTO `sys_dict_type` (`dict_type`, `dict_type_name`, `dict_type_desc`, `create_time`, `update_time`)
VALUES ('import_status', '导入状态', '导入任务执行状态', NOW(), NOW()) ON DUPLICATE KEY UPDATE dict_type_name = '导入状态';

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_desc`, `sort`, `status`, `create_time`, `update_time`) VALUES
('import_status', 'NONE', '未开始', '导入任务未开始执行', 1, 1, NOW(), NOW()),
('import_status', 'EXECUTING', '执行中', '导入任务正在执行', 2, 1, NOW(), NOW()),
('import_status', 'COMPLETED', '已完成', '导入任务执行完成', 3, 1, NOW(), NOW()),
('import_status', 'FAILED', '失败', '导入任务执行失败', 4, 1, NOW(), NOW()),
('import_status', 'CANCELLED', '已取消', '导入任务已被取消', 5, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), dict_desc = VALUES(dict_desc);

-- 数据字典：错误类型
INSERT INTO `sys_dict_type` (`dict_type`, `dict_type_name`, `dict_type_desc`, `create_time`, `update_time`)
VALUES ('error_type', '错误类型', '导入错误类型分类', NOW(), NOW()) ON DUPLICATE KEY UPDATE dict_type_name = '错误类型';

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `dict_desc`, `sort`, `status`, `create_time`, `update_time`) VALUES
('error_type', 'VALIDATION', '验证错误', '数据验证失败', 1, 1, NOW(), NOW()),
('error_type', 'BUSINESS', '业务错误', '业务处理失败', 2, 1, NOW(), NOW()),
('error_type', 'SYSTEM', '系统错误', '系统异常导致的错误', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), dict_desc = VALUES(dict_desc);

-- 创建索引优化查询性能
CREATE INDEX idx_import_record_composite ON ef_async_import_record(app_id, import_status, create_time);
CREATE INDEX idx_import_error_register ON ef_import_error_detail(register_id, error_type);
CREATE INDEX idx_import_template_app ON ef_import_template(app_id, is_deleted);

-- 添加表注释
ALTER TABLE ef_async_import_record COMMENT = '异步导入记录表 - 记录导入任务的基本信息和执行状态';
ALTER TABLE ef_import_error_detail COMMENT = '导入错误详情表 - 记录详细的错误信息和位置';
ALTER TABLE ef_import_template COMMENT = '导入模板表 - 定义导入模板和验证规则';
ALTER TABLE ef_import_executor COMMENT = '导入任务执行器表 - 配置导入业务处理逻辑';
