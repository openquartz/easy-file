package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.processor.ApplicationContentPostProcessor;
import org.svnee.easyfile.starter.processor.EasyFileBeanEnhancePostProcessor;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.file.UploadService;
import org.svnee.easyfile.storage.file.local.LocalUploadServiceImpl;

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

    @Bean
    public EasyFileBeanEnhancePostProcessor fileExportExecutorPostProcessor() {
        return new EasyFileBeanEnhancePostProcessor();
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
