package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.BaseAsyncImportHandler;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.service.IdempotentImportManager;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncImportHandlerThreadPoolProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.starter.trigger.handler.DefaultAsyncImportHandler;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DefaultAsyncImportHandlerAutoConfiguration
 *
 * @author svnee
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncImportHandlerThreadPoolProperties.class, EasyFileImportProperties.class})
@ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(ImportStorageService.class)
@AutoConfigureAfter({EasyFileImportAutoConfiguration.class, EasyFileCreatorAutoConfiguration.class})
public class DefaultAsyncImportHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncImportHandler.class)
    @ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "trigger-type", havingValue = "thread_pool",
        matchIfMissing = true)
    public BaseAsyncImportHandler defaultAsyncImportHandler(
        ImportStorageService importStorageService,
        ObjectProvider<IdempotentImportManager> idempotentImportManager,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler,
        DefaultAsyncImportHandlerThreadPoolProperties defaultAsyncImportHandlerThreadPoolProperties) {
        return new DefaultAsyncImportHandler(
            importStorageService,
            idempotentImportManager.getIfAvailable(),
            baseDefaultDownloadRejectExecutionHandler,
            defaultAsyncImportHandlerThreadPoolProperties);
    }
}
