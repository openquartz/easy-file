package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandler;
import com.openquartz.easyfile.core.executor.BaseAsyncFileExportHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.trigger.handler.DefaultAsyncFileTriggerExecuteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.core.executor.BaseDefaultRejectExecutionHandler;

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
    @ConditionalOnMissingBean(BaseAsyncFileExportHandler.class)
    public AsyncFileTriggerExecuteHandler defaultAsyncFileHandler(BaseDefaultRejectExecutionHandler baseDefaultRejectExecutionHandler,
                                                                  DefaultAsyncHandlerThreadPoolProperties defaultAsyncHandlerThreadPoolProperties) {
        return new DefaultAsyncFileTriggerExecuteHandler(null, baseDefaultRejectExecutionHandler, defaultAsyncHandlerThreadPoolProperties);
    }

}
