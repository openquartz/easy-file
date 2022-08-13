package org.svnee.easyfile.starter.spring.boot.autoconfig;

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
import org.svnee.easyfile.starter.aop.FileExportExecutorAnnotationAdvisor;
import org.svnee.easyfile.starter.aop.FileExportInterceptor;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.processor.AutoRegisteredDownloadTaskListener;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileRemoteProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileDownloadProperties.class, EasyFileRemoteProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class EasyFileEnhanceCreatorAutoConfiguration {

    @Bean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor fileExportExecutorAnnotationAdvisor(EasyFileDownloadProperties easyFileDownloadProperties,
        DownloadStorageService downloadStorageService,
        LimitingService limitingService,
        BaseAsyncFileHandler baseAsyncFileHandler,
        ApplicationContext applicationContext
    ) {
        FileExportInterceptor interceptor = new FileExportInterceptor(easyFileDownloadProperties, limitingService,
            downloadStorageService, baseAsyncFileHandler, applicationContext);
        FileExportExecutorAnnotationAdvisor advisor = new FileExportExecutorAnnotationAdvisor(interceptor);
        advisor.setOrder(easyFileDownloadProperties.getExportAdvisorOrder());
        return advisor;
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")
    public AutoRegisteredDownloadTaskListener autoRegisteredDownloadTaskListener(
        EasyFileDownloadProperties easyFileDownloadProperties,
        DownloadStorageService downloadStorageService) {
        return new AutoRegisteredDownloadTaskListener(easyFileDownloadProperties, downloadStorageService);
    }

}
