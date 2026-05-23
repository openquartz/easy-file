package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.ImportResultFileGenerator;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.core.executor.impl.DefaultImportExecutor;
import com.openquartz.easyfile.server.service.ImportService;
import com.openquartz.easyfile.server.service.impl.ImportServiceImpl;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.storage.file.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EasyFile导入功能自动配置类
 * 自动配置导入相关的Bean和服务
 *
 * @author svnee
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileImportProperties.class)
@ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileImportAutoConfiguration {

    /**
     * 导入结果文件生成器
     * 用于生成包含统计信息和错误记录的Excel文件
     */
    @Bean
    @ConditionalOnMissingBean(ImportResultFileGenerator.class)
    public ImportResultFileGenerator importResultFileGenerator(UploadService uploadService,
                                                              EasyFileImportProperties importProperties) {
        log.info("初始化ImportResultFileGenerator，结果文件路径: {}", importProperties.getResultFilePath());
        return new ImportResultFileGenerator(uploadService);
    }

    /**
     * 默认导入执行器
     * 提供基础的导入处理逻辑
     */
    @Bean
    @ConditionalOnMissingBean(BaseImportExecutor.class)
    public BaseImportExecutor<?> defaultImportExecutor(EasyFileImportProperties importProperties) {
        log.info("初始化DefaultImportExecutor，分页大小: {}，最大重试次数: {}",
            importProperties.getPageSize(), importProperties.getMaxRetry());
        return new DefaultImportExecutor<>();
    }

    /**
     * 导入服务
     * 提供导入任务管理的核心业务逻辑
     */
    @Bean
    @ConditionalOnMissingBean(ImportService.class)
    public ImportService importService(EasyFileImportProperties importProperties) {
        log.info("初始化ImportServiceImpl，异步执行: {}，断点续传: {}",
            importProperties.isEnableAsync(), importProperties.isSupportResumable());
        return new ImportServiceImpl();
    }

    /**
     * 导入配置验证器
     * 验证导入配置的有效性
     */
    @Bean
    public EasyFileImportConfigValidator importConfigValidator() {
        return new EasyFileImportConfigValidator();
    }

    /**
     * 导入配置验证器
     */
    public static class EasyFileImportConfigValidator {

        /**
         * 验证配置
         */
        public void validate(EasyFileImportProperties properties) {
            if (properties.getPageSize() <= 0) {
                throw new IllegalArgumentException("分页大小必须大于0，当前值: " + properties.getPageSize());
            }

            if (properties.getMaxRetry() < 0) {
                throw new IllegalArgumentException("最大重试次数不能为负数，当前值: " + properties.getMaxRetry());
            }

            if (properties.getTimeout() <= 0) {
                throw new IllegalArgumentException("超时时间必须大于0，当前值: " + properties.getTimeout());
            }

            if (properties.getMaxConcurrentTasks() <= 0) {
                throw new IllegalArgumentException("最大并发任务数必须大于0，当前值: " + properties.getMaxConcurrentTasks());
            }

            if (properties.getProgressUpdateInterval() <= 0) {
                throw new IllegalArgumentException("进度更新间隔必须大于0，当前值: " + properties.getProgressUpdateInterval());
            }

            // 验证Excel解析配置
            EasyFileImportProperties.ExcelParser excelParser = properties.getExcelParser();
            if (excelParser.getStreamingBufferSize() <= 0) {
                throw new IllegalArgumentException("流式读取缓冲区大小必须大于0，当前值: " + excelParser.getStreamingBufferSize());
            }

            if (excelParser.getMaxRows() <= 0) {
                throw new IllegalArgumentException("最大行数必须大于0，当前值: " + excelParser.getMaxRows());
            }

            // 验证业务处理配置
            EasyFileImportProperties.BusinessProcess businessProcess = properties.getBusinessProcess();
            if (businessProcess.getBatchSize() <= 0) {
                throw new IllegalArgumentException("批量处理大小必须大于0，当前值: " + businessProcess.getBatchSize());
            }

            log.info("EasyFile导入配置验证通过");
        }
    }
}
