package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.starter.aop.FileExportExecutorAnnotationAdvisor;
import org.svnee.easyfile.starter.aop.FileExportInterceptor;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.processor.AutoRegisteredDownloadTaskListener;
import org.svnee.easyfile.starter.processor.FileExportExecutorPostProcessor;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;
import org.svnee.easyfile.storage.file.UploadService;
import org.svnee.easyfile.storage.file.local.LocalUploadService;
import org.svnee.easyfile.storage.impl.LocalDownloadStorageService;
import org.svnee.easyfile.storage.impl.LocalLimitingService;
import org.svnee.easyfile.storage.impl.RemoteDownloadStorageService;
import org.svnee.easyfile.storage.impl.RemoteLimitingService;

/**
 * @author svnee
 * @desc spring-配置核心类
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncHandlerThreadPoolProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileCreatorAutoConfiguration {

    @Bean
    public FileExportExecutorPostProcessor fileExportExecutorPostProcessor() {
        return new FileExportExecutorPostProcessor();
    }

    @Bean
    @ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enableAutoRegister", havingValue = "true")
    public AutoRegisteredDownloadTaskListener autoRegisteredDownloadTaskListener(
        EasyFileDownloadProperties easyFileDownloadProperties,
        DownloadStorageService downloadStorageService) {
        return new AutoRegisteredDownloadTaskListener(easyFileDownloadProperties, downloadStorageService);
    }

    @Bean
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
    @ConditionalOnMissingBean(BaseDefaultDownloadRejectExecutionHandler.class)
    public BaseDefaultDownloadRejectExecutionHandler defaultDownloadRejectExecutionHandler() {
        return new DefaultDownloadRejectExecutionHandler();
    }

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

    @Bean
    @ConditionalOnMissingBean(UploadService.class)
    public UploadService localUploadService() {
        return new LocalUploadService();
    }

    @Bean
    @ConditionalOnClass(LocalDownloadStorageService.class)
    public DownloadStorageService localDownloadStorageService() {
        return new LocalDownloadStorageService();
    }

    @Bean
    @ConditionalOnClass(RemoteDownloadStorageService.class)
    public DownloadStorageService remoteDownloadStorageService() {
        return new RemoteDownloadStorageService();
    }

    @Bean
    @ConditionalOnClass(LocalLimitingService.class)
    public LimitingService localLimitingService() {
        return new LocalLimitingService();
    }

    @Bean
    @ConditionalOnClass(RemoteLimitingService.class)
    public LimitingService remoteLimitingService() {
        return new RemoteLimitingService();
    }

}
