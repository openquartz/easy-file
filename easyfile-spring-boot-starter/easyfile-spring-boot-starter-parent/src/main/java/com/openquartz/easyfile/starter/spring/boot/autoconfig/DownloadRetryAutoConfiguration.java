package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.starter.trigger.retry.DownloadRetryScanner;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EasyFileDownloadRetryProperties.class, EasyFileDownloadProperties.class})
@ConditionalOnBean({BaseAsyncFileHandler.class, DownloadStorageService.class})
@ConditionalOnProperty(prefix = EasyFileDownloadRetryProperties.PREFIX, name = "scan-enabled", havingValue = "true",
    matchIfMissing = true)
@AutoConfigureAfter({DefaultAsyncFileHandlerAutoConfiguration.class, ScheduleAsyncFileHandlerAutoConfiguration.class,
    DisruptorAsyncFileHandlerAutoConfiguration.class, RocketMqAsyncFileHandlerAutoConfiguration.class})
public class DownloadRetryAutoConfiguration implements DisposableBean {

    private ScheduledThreadPoolExecutor executor;

    @Bean
    public DownloadRetryScanner downloadRetryScanner(DownloadStorageService downloadStorageService,
        BaseAsyncFileHandler baseAsyncFileHandler, EasyFileDownloadRetryProperties properties,
        EasyFileDownloadProperties downloadProperties) {
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, baseAsyncFileHandler,
            properties, downloadProperties);
        this.executor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("DownloadRetryScanner-thread-%d").build());
        this.executor.scheduleAtFixedRate(scanner::scan, properties.getScanPeriodSeconds(),
            properties.getScanPeriodSeconds(), TimeUnit.SECONDS);
        return scanner;
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
