package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.svnee.easyfile.starter.executor.impl.MqTriggerAsyncFileHandler;
import org.svnee.easyfile.starter.executor.trigger.DisruptorTriggerConsumer;
import org.svnee.easyfile.starter.executor.trigger.DisruptorTriggerProducer;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerHandler;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerProducer;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * Disruptor
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DisruptorAsyncHandlerProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "disruptor")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class DisruptorAsyncFileHandlerAutoConfiguration {

    @Bean
    @Primary
    public MqTriggerAsyncFileHandler mqTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        DownloadTriggerService downloadTriggerService,
        MQTriggerProducer mqTriggerProducer,
        DisruptorAsyncHandlerProperties mqAsyncHandlerProperties) {
        return new MqTriggerAsyncFileHandler(easyFileDownloadProperties,
            uploadService, downloadStorageService,
            downloadTriggerService,
            mqAsyncHandlerProperties,
            mqTriggerProducer);
    }

    @Bean
    @ConditionalOnMissingBean(MQTriggerHandler.class)
    public MQTriggerHandler mqTriggerHandler(MqTriggerAsyncFileHandler mqTriggerAsyncFileHandler) {
        return mqTriggerAsyncFileHandler;
    }

    @Bean
    @ConditionalOnMissingBean(value = MQTriggerProducer.class, name = "mqTriggerProducer")
    public DisruptorTriggerProducer mqTriggerProducer(DisruptorAsyncHandlerProperties mqAsyncHandlerProperties) {
        return new DisruptorTriggerProducer(mqAsyncHandlerProperties);
    }

    @Bean
    @ConditionalOnMissingBean(DisruptorTriggerConsumer.class)
    public DisruptorTriggerConsumer disruptorTriggerConsumer(EasyFileDownloadProperties downloadProperties,
        MQTriggerHandler mqTriggerHandler,
        DisruptorTriggerProducer mqTriggerProducer) {
        DisruptorTriggerConsumer consumer = new DisruptorTriggerConsumer(downloadProperties,
            mqTriggerHandler);
        mqTriggerProducer.registerAndStart(consumer);
        return consumer;
    }

}
