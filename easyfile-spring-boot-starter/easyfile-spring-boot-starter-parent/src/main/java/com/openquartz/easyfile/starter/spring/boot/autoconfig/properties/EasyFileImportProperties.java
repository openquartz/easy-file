package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EasyFile导入功能配置属性
 *
 * @author svnee
 */
@Data
@ConfigurationProperties(prefix = EasyFileImportProperties.PREFIX)
public class EasyFileImportProperties implements IDatabaseAsyncHandlerProperty {

    public static final String PREFIX = "easyfile.import";

    /**
     * 是否启用导入功能
     */
    private boolean enabled = true;

    /**
     * 异步触发类型: thread_pool | db_schedule
     */
    private ImportAsyncTriggerType triggerType = ImportAsyncTriggerType.THREAD_POOL;

    /**
     * 同步路径是否先持久化注册
     */
    private boolean syncPersist = true;

    /**
     * 失败重试是否从断点续传
     */
    private boolean retryFromResume = true;

    /**
     * DB 调度周期（秒）
     */
    private Integer schedulePeriodSeconds = 10;

    /**
     * 最大触发次数
     */
    private Integer maxTriggerCount = 3;

    /**
     * 执行超时时间（秒）
     */
    private Integer executingTimeoutSeconds = 3600;

    /**
     * 回溯小时数
     */
    private Integer lookBackHours = 2;

    /**
     * 最大归档小时
     */
    private Integer maxArchiveHours = 24;

    /**
     * 一次触发处理条数
     */
    private Integer triggerOffset = 50;

    /**
     * 最小收割时间（秒）
     */
    private Integer minReaperSeconds = 3600;

    @Override
    public Integer getSchedulePeriod() {
        return schedulePeriodSeconds;
    }

    @Override
    public Integer getMaxTriggerCount() {
        return maxTriggerCount;
    }

    @Override
    public Integer getMaxExecuteTimeout() {
        return executingTimeoutSeconds;
    }

    @Override
    public Integer getLookBackHours() {
        return lookBackHours;
    }

    @Override
    public Integer getMaxArchiveHours() {
        return maxArchiveHours;
    }

    /**
     * 默认分页大小
     */
    private int pageSize = 1000;

    /**
     * 默认最大重试次数
     */
    private int maxRetry = 3;

    /**
     * 默认超时时间(毫秒)
     */
    private long timeout = 3600000L; // 1小时

    /**
     * 结果文件存储路径
     */
    private String resultFilePath = "/tmp/easyfile/import/";

    /**
     * 是否启用异步执行
     */
    private boolean enableAsync = true;

    /**
     * 是否支持断点续传
     */
    private boolean supportResumable = true;

    /**
     * 是否启用幂等性检查
     */
    private boolean enableIdempotent = true;

    /**
     * 文件哈希算法: MD5, SHA-256
     */
    private String hashAlgorithm = "MD5";

    /**
     * 临时文件清理时间(小时)
     */
    private int tempFileCleanupHours = 24;

    /**
     * 最大并发导入任务数
     */
    private int maxConcurrentTasks = 10;

    /**
     * 导入进度更新间隔(毫秒)
     */
    private long progressUpdateInterval = 1000L;

    /**
     * 是否启用详细日志
     */
    private boolean enableDetailLog = false;

    /**
     * Excel解析相关配置
     */
    private ExcelParser excelParser = new ExcelParser();

    /**
     * Excel解析器配置
     */
    @Data
    public static class ExcelParser {

        /**
         * 是否启用流式读取(防OOM)
         */
        private boolean enableStreaming = true;

        /**
         * 流式读取缓冲区大小
         */
        private int streamingBufferSize = 1000;

        /**
         * 最大行数限制
         */
        private long maxRows = 1000000L;

        /**
         * 是否跳过空行
         */
        private boolean skipEmptyRows = true;

        /**
         * 是否严格模式(严格检查数据类型)
         */
        private boolean strictMode = false;
    }

    /**
     * 验证相关配置
     */
    private Validation validation = new Validation();

    /**
     * 验证配置
     */
    @Data
    public static class Validation {

        /**
         * 是否启用数据验证
         */
        private boolean enabled = true;

        /**
         * 是否验证必填字段
         */
        private boolean validateRequired = true;

        /**
         * 是否验证数据类型
         */
        private boolean validateDataType = true;

        /**
         * 是否验证数据范围
         */
        private boolean validateRange = true;

        /**
         * 是否启用业务规则验证
         */
        private boolean validateBusinessRules = true;

        /**
         * 最大错误记录数(超过则停止导入)
         */
        private int maxErrorCount = 1000;
    }

    /**
     * 业务处理相关配置
     */
    private BusinessProcess businessProcess = new BusinessProcess();

    /**
     * 业务处理配置
     */
    @Data
    public static class BusinessProcess {

        /**
         * 是否启用事务
         */
        private boolean enableTransaction = true;

        /**
         * 事务隔离级别
         */
        private String transactionIsolation = "READ_COMMITTED";

        /**
         * 事务传播行为
         */
        private String transactionPropagation = "REQUIRED";

        /**
         * 是否批量处理
         */
        private boolean enableBatch = true;

        /**
         * 批量处理大小
         */
        private int batchSize = 100;

        /**
         * 是否启用数据转换
         */
        private boolean enableDataTransform = true;

        /**
         * 是否启用数据清洗
         */
        private boolean enableDataCleaning = true;
    }

    /**
     * 通知相关配置
     */
    private Notification notification = new Notification();

    /**
     * 通知配置
     */
    @Data
    public static class Notification {

        /**
         * 是否启用通知
         */
        private boolean enabled = true;

        /**
         * 是否通知成功结果
         */
        private boolean notifySuccess = true;

        /**
         * 是否通知失败结果
         */
        private boolean notifyFailure = true;

        /**
         * 是否通知进度更新
         */
        private boolean notifyProgress = false;

        /**
         * 进度通知阈值(百分比)
         */
        private int progressThreshold = 10;

        /**
         * 通知方式: EMAIL, SMS, WEBHOOK
         */
        private String notifyMethod = "EMAIL";

        /**
         * Webhook URL
         */
        private String webhookUrl = "";
    }
}
