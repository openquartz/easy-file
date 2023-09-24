package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.executor.BaseDefaultExportRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.impl.DefaultExportRejectExecutionHandler;
import com.openquartz.easyfile.core.metrics.ExportMetricsListener;
import com.openquartz.easyfile.core.metrics.MetricsListener;
import com.openquartz.easyfile.metrics.api.config.MetricsConfig;
import com.openquartz.easyfile.metrics.api.metric.MetricsTrackerFacade;
import com.openquartz.easyfile.starter.init.EasyFileInitializingEntrance;
import com.openquartz.easyfile.starter.processor.ApplicationContentPostProcessor;
import com.openquartz.easyfile.starter.processor.DownloadInterceptorPostProcessor;
import com.openquartz.easyfile.starter.processor.EasyFileBeanEnhancePostProcessor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileMetricsProperties;
import com.openquartz.easyfile.storage.file.UploadService;
import com.openquartz.easyfile.storage.file.local.LocalUploadServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring-配置核心类
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({EasyFileDownloadProperties.class, EasyFileMetricsProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyFileCreatorAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public EasyFileInitializingEntrance easyEventInitializingEntrance() {
        return new EasyFileInitializingEntrance();
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
    @ConditionalOnMissingBean(BaseDefaultExportRejectExecutionHandler.class)
    public BaseDefaultExportRejectExecutionHandler defaultDownloadRejectExecutionHandler() {
        return new DefaultExportRejectExecutionHandler();
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

    @Bean(destroyMethod = "close")
    public MetricsTrackerFacade metricsTrackerFacade(EasyFileMetricsProperties easyFileMetricsProperties) {
        MetricsConfig metricsConfig = new MetricsConfig();
        metricsConfig.setMetricsName(easyFileMetricsProperties.getName());
        metricsConfig.setHost(easyFileMetricsProperties.getHost());
        metricsConfig.setPort(easyFileMetricsProperties.getPort());
        metricsConfig.setJmxConfig(easyFileMetricsProperties.getJmxConfig());
        metricsConfig.setProps(easyFileMetricsProperties.getProps());

        if (StringUtils.isNotBlank(metricsConfig.getMetricsName())) {
            MetricsTrackerFacade facade = new MetricsTrackerFacade();
            facade.start(metricsConfig);
            return facade;
        }
        return null;
    }

    @Bean
    public MetricsListener metricsListener(){
        return new MetricsListener();
    }

    @Bean
    public ExportMetricsListener downloadMetricsListener(){
        return new ExportMetricsListener();
    }

}
