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
import org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * DefaultAsyncFileHandlerAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncHandlerThreadPoolProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "default")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class DefaultAsyncFileHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileHandler.class)
    public BaseAsyncFileHandler defaultAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler,
        DefaultAsyncHandlerThreadPoolProperties defaultAsyncHandlerThreadPoolProperties) {
        return new DefaultAsyncFileHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            baseDefaultDownloadRejectExecutionHandler,
            defaultAsyncHandlerThreadPoolProperties);
    }

}
