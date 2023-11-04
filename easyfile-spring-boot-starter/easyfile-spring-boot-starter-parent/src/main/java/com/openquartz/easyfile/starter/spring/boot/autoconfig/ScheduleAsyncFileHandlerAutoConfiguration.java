package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.BaseAsyncFileExportHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import com.openquartz.easyfile.starter.trigger.handler.ScheduleTriggerAsyncFileExportHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.core.executor.BaseDefaultExportRejectExecutionHandler;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;

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
    public BaseAsyncFileExportHandler scheduleTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
                                                                      UploadService uploadService,
                                                                      DownloadStorageService downloadStorageService,
                                                                      FileTriggerService fileTriggerService,
                                                                      BaseDefaultExportRejectExecutionHandler baseDefaultExportRejectExecutionHandler,
                                                                      ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties) {
        return new ScheduleTriggerAsyncFileExportHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            fileTriggerService,
            scheduleAsyncHandlerProperties,
            baseDefaultExportRejectExecutionHandler);
    }

}
