package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.core.executor.BaseAsyncImportHandler;
import com.openquartz.easyfile.core.intercept.DownloadExecutorInterceptor;
import com.openquartz.easyfile.core.intercept.I18nDownloadExecutorInterceptor;
import com.openquartz.easyfile.starter.aop.FileExportExecutorAnnotationAdvisor;
import com.openquartz.easyfile.starter.aop.FileExportInterceptor;
import com.openquartz.easyfile.starter.aop.FileImportExecutorAnnotationAdvisor;
import com.openquartz.easyfile.starter.aop.FileImportInterceptor;
import com.openquartz.easyfile.starter.processor.AutoRegisteredDownloadTaskListener;
import com.openquartz.easyfile.starter.processor.AutoRegisteredImportTaskListener;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.condition.DownloadOrImportEnabledCondition;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileDownloadProperties.class, EasyFileImportProperties.class})
@Conditional(DownloadOrImportEnabledCondition.class)
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class EasyFileEnhanceCreatorAutoConfiguration {

    @Bean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean({DownloadStorageService.class, BaseAsyncFileHandler.class, LimitingService.class})
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
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
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean({ImportStorageService.class, BaseAsyncImportHandler.class, LimitingService.class})
    @ConditionalOnProperty(prefix = EasyFileImportProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
    public Advisor fileImportExecutorAnnotationAdvisor(EasyFileImportProperties easyFileImportProperties,
                                                       EasyFileDownloadProperties easyFileDownloadProperties,
                                                       ImportStorageService importStorageService,
                                                       LimitingService limitingService,
                                                       BaseAsyncImportHandler baseAsyncImportHandler,
                                                       ApplicationContext applicationContext
    ) {
        FileImportInterceptor interceptor = new FileImportInterceptor(easyFileImportProperties,
            easyFileDownloadProperties, limitingService, importStorageService, baseAsyncImportHandler,
            applicationContext);
        FileImportExecutorAnnotationAdvisor advisor = new FileImportExecutorAnnotationAdvisor(interceptor);
        advisor.setOrder(Integer.MAX_VALUE);
        return advisor;
    }

    @Bean
    @ConditionalOnBean(DownloadStorageService.class)
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
    public DownloadExecutorInterceptor i18nDownloadExecutorInterceptor(DownloadStorageService downloadStorageService) {
        return new I18nDownloadExecutorInterceptor(downloadStorageService);
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")
    public AutoRegisteredDownloadTaskListener autoRegisteredDownloadTaskListener(
            EasyFileDownloadProperties easyFileDownloadProperties,
            DownloadStorageService downloadStorageService) {
        return new AutoRegisteredDownloadTaskListener(easyFileDownloadProperties, downloadStorageService);
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")
    public AutoRegisteredImportTaskListener autoRegisteredImportTaskListener(
        EasyFileDownloadProperties easyFileDownloadProperties,
        ImportStorageService importStorageService
    ) {
        return new AutoRegisteredImportTaskListener(easyFileDownloadProperties, importStorageService);
    }

}
