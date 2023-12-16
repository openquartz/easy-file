package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.trigger.DisruptorTriggerConsumer;
import com.openquartz.easyfile.starter.trigger.DisruptorTriggerProducer;
import com.openquartz.easyfile.starter.trigger.handler.MqTriggerDefaultAsyncFileExportHandler;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    public MqTriggerDefaultAsyncFileExportHandler mqTriggerAsyncFileHandler(DownloadStorageService downloadStorageService,
                                                                            FileTriggerService fileTriggerService,
                                                                            MQTriggerProducer mqTriggerProducer,
                                                                            DisruptorAsyncHandlerProperties mqAsyncHandlerProperties,
                                                                            AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory) {

        return new MqTriggerDefaultAsyncFileExportHandler(downloadStorageService,
                fileTriggerService,
                mqAsyncHandlerProperties,
                mqTriggerProducer,
                asyncFileTriggerExecuteHandlerFactory);
    }

    @Bean
    @ConditionalOnMissingBean(MQTriggerHandler.class)
    public MQTriggerHandler mqTriggerHandler(MqTriggerDefaultAsyncFileExportHandler mqTriggerAsyncFileHandler) {
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
