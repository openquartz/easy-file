package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.AsyncImportHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncImportHandlerProperties;
import com.openquartz.easyfile.starter.trigger.handler.ScheduleTriggerAsyncImportHandler;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import com.openquartz.easyfile.storage.importer.ImportTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Schedule Async Import Handler AutoConfiguration
 *
 * @author svnee
 */
@Slf4j
@Configuration
@ConditionalOnBean(ImportTriggerService.class)
@EnableConfigurationProperties({ScheduleAsyncImportHandlerProperties.class})
@ConditionalOnProperty(prefix = ScheduleAsyncImportHandlerProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class ScheduleAsyncImportHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ScheduleTriggerAsyncImportHandler.class)
    public ScheduleTriggerAsyncImportHandler scheduleTriggerAsyncImportHandler(
        ImportTriggerService importTriggerService,
        ImportStorageService importStorageService,
        AsyncImportHandler asyncImportHandler,
        ScheduleAsyncImportHandlerProperties scheduleAsyncImportHandlerProperties) {
        return new ScheduleTriggerAsyncImportHandler(importTriggerService, importStorageService, asyncImportHandler,
            scheduleAsyncImportHandlerProperties);
    }

}
