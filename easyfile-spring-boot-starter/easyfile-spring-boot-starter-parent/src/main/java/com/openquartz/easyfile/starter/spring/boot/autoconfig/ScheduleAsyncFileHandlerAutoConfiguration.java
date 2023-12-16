package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.BaseAsyncFileExportHandler;
import com.openquartz.easyfile.core.executor.BaseDefaultRejectExecutionHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import com.openquartz.easyfile.starter.trigger.handler.ScheduleTriggerDefaultAsyncFileExportHandler;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disruptor
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@ConditionalOnBean(FileTriggerService.class)
@EnableConfigurationProperties({ScheduleAsyncHandlerProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "schedule")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class ScheduleAsyncFileHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileExportHandler.class)
    public ScheduleTriggerDefaultAsyncFileExportHandler scheduleTriggerAsyncFileHandler(DownloadStorageService downloadStorageService,
                                                                      FileTriggerService fileTriggerService,
                                                                      BaseDefaultRejectExecutionHandler baseDefaultRejectExecutionHandler,
                                                                      AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory,
                                                                      ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties) {
       return new ScheduleTriggerDefaultAsyncFileExportHandler(downloadStorageService,
                fileTriggerService,
                scheduleAsyncHandlerProperties,
                asyncFileTriggerExecuteHandlerFactory,
                baseDefaultRejectExecutionHandler);
    }

}
