package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.aop.FileExportInterceptor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import com.openquartz.easyfile.starter.aop.FileExportExecutorAnnotationAdvisor;
import com.openquartz.easyfile.core.executor.BaseAsyncFileExportHandler;
import com.openquartz.easyfile.starter.processor.AutoRegisteredFileTaskListener;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;
import com.openquartz.easyfile.storage.download.LimitingService;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class EasyFileEnhanceCreatorAutoConfiguration {

    @Bean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor fileExportExecutorAnnotationAdvisor(EasyFileDownloadProperties easyFileDownloadProperties,
                                                       FileTaskStorageService fileTaskStorageService,
                                                       LimitingService limitingService,
                                                       BaseAsyncFileExportHandler baseAsyncFileHandler,
                                                       ApplicationContext applicationContext
    ) {
        FileExportInterceptor interceptor = new FileExportInterceptor(easyFileDownloadProperties, limitingService,
                fileTaskStorageService, baseAsyncFileHandler, applicationContext);
        FileExportExecutorAnnotationAdvisor advisor = new FileExportExecutorAnnotationAdvisor(interceptor);
        advisor.setOrder(easyFileDownloadProperties.getExportAdvisorOrder());
        return advisor;
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")
    public AutoRegisteredFileTaskListener autoRegisteredDownloadTaskListener(
            EasyFileDownloadProperties easyFileDownloadProperties,
            FileTaskStorageService fileTaskStorageService) {
        return new AutoRegisteredFileTaskListener(easyFileDownloadProperties, fileTaskStorageService);
    }

}
