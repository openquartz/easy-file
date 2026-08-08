package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.BaseAsyncImportHandler;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.service.IdempotentImportManager;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.starter.trigger.handler.ScheduleTriggerAsyncImportHandler;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.imports.ImportTriggerService;
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
 * ScheduleAsyncImportHandlerAutoConfiguration
 *
 * @author svnee
 */
@Slf4j
@Configuration
@ConditionalOnBean({ImportTriggerService.class, ImportStorageService.class})
@EnableConfigurationProperties({EasyFileImportProperties.class})
@ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({EasyFileImportAutoConfiguration.class, EasyFileCreatorAutoConfiguration.class})
public class ScheduleAsyncImportHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncImportHandler.class)
    @ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "trigger-type", havingValue = "db_schedule")
    public BaseAsyncImportHandler scheduleTriggerAsyncImportHandler(
        EasyFileImportProperties easyFileImportProperties,
        ImportStorageService importStorageService,
        ImportTriggerService importTriggerService,
        ObjectProvider<IdempotentImportManager> idempotentImportManager,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler) {
        return new ScheduleTriggerAsyncImportHandler(
            easyFileImportProperties,
            importStorageService,
            importTriggerService,
            idempotentImportManager.getIfAvailable(),
            baseDefaultDownloadRejectExecutionHandler);
    }
}
