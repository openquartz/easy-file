package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.impl.ScheduleTriggerAsyncFileHandler;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * Disruptor
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({ScheduleAsyncHandlerProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "schedule")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class ScheduleAsyncFileHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileHandler.class)
    public BaseAsyncFileHandler scheduleTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        DownloadTriggerService downloadTriggerService,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler,
        ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties) {
        return new ScheduleTriggerAsyncFileHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            downloadTriggerService,
            scheduleAsyncHandlerProperties,
            baseDefaultDownloadRejectExecutionHandler);
    }

}
