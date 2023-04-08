package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.init.EasyFileInitializingEntrance;
import com.openquartz.easyfile.starter.processor.ApplicationContentPostProcessor;
import com.openquartz.easyfile.starter.processor.DownloadInterceptorPostProcessor;
import com.openquartz.easyfile.starter.processor.EasyFileBeanEnhancePostProcessor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.impl.DefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.storage.file.UploadService;
import com.openquartz.easyfile.storage.file.local.LocalUploadServiceImpl;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(EasyFileDownloadProperties.class)
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileCreatorAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public EasyFileInitializingEntrance easyEventInitializingEntrance(
        EasyFileDownloadProperties easyFileDownloadProperties) {
        return new EasyFileInitializingEntrance(easyFileDownloadProperties);
    }

    @Bean
    public EasyFileBeanEnhancePostProcessor fileExportExecutorPostProcessor() {
        return new EasyFileBeanEnhancePostProcessor();
    }

    @Bean
    public DownloadInterceptorPostProcessor downloadInterceptorPostProcessor() {
        return new DownloadInterceptorPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(BaseDefaultDownloadRejectExecutionHandler.class)
    public BaseDefaultDownloadRejectExecutionHandler defaultDownloadRejectExecutionHandler() {
        return new DefaultDownloadRejectExecutionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(UploadService.class)
    public UploadService localUploadService() {
        return new LocalUploadServiceImpl();
    }

    @Bean
    public ApplicationContentPostProcessor applicationContentPostProcessor(ApplicationContext applicationContext) {
        return new ApplicationContentPostProcessor(applicationContext);
    }

}
