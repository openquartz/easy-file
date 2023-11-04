package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.trigger.DisruptorTriggerConsumer;
import com.openquartz.easyfile.starter.trigger.DisruptorTriggerProducer;
import com.openquartz.easyfile.starter.trigger.handler.MqTriggerAsyncFileExportHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * Disruptor
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@ConditionalOnBean(FileTriggerService.class)
@EnableConfigurationProperties({DisruptorAsyncHandlerProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "disruptor")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class DisruptorAsyncFileHandlerAutoConfiguration {

    @Bean
    @Primary
    public MqTriggerAsyncFileExportHandler mqTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
                                                                     UploadService uploadService,
                                                                     DownloadStorageService downloadStorageService,
                                                                     FileTriggerService fileTriggerService,
                                                                     MQTriggerProducer mqTriggerProducer,
                                                                     DisruptorAsyncHandlerProperties mqAsyncHandlerProperties) {
        return new MqTriggerAsyncFileExportHandler(easyFileDownloadProperties,
            uploadService, downloadStorageService,
            fileTriggerService,
            mqAsyncHandlerProperties,
            mqTriggerProducer);
    }

    @Bean
    @ConditionalOnMissingBean(MQTriggerHandler.class)
    public MQTriggerHandler mqTriggerHandler(MqTriggerAsyncFileExportHandler mqTriggerAsyncFileHandler) {
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