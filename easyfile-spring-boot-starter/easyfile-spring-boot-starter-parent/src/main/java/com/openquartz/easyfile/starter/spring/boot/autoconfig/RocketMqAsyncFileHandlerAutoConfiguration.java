package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DisruptorAsyncHandlerProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.RocketMqAsyncHandlerProperties;
import com.openquartz.easyfile.starter.trigger.RocketMQTriggerConsumer;
import com.openquartz.easyfile.starter.trigger.RocketMQTriggerProducer;
import com.openquartz.easyfile.starter.trigger.handler.MqTriggerDefaultAsyncFileExportHandler;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.UtilAll;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author svnee
 **/
@Slf4j
@Configuration
@ConditionalOnBean(FileTriggerService.class)
@ConditionalOnClass(MQProducer.class)
@EnableConfigurationProperties({RocketMqAsyncHandlerProperties.class})
@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "async-trigger-type", havingValue = "rocketmq")
@AutoConfigureAfter(EasyFileCreatorAutoConfiguration.class)
public class RocketMqAsyncFileHandlerAutoConfiguration {

    @Bean
    @Primary
    public MqTriggerDefaultAsyncFileExportHandler mqTriggerAsyncFileHandler(FileTaskStorageService fileTaskStorageService,
                                                                            FileTriggerService fileTriggerService,
                                                                            MQTriggerProducer mqTriggerProducer,
                                                                            DisruptorAsyncHandlerProperties mqAsyncHandlerProperties,
                                                                            AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory) {

        return new MqTriggerDefaultAsyncFileExportHandler(fileTaskStorageService,
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
    public MQTriggerProducer mqTriggerProducer(RocketMqAsyncHandlerProperties mqAsyncHandlerProperties,
        MQProducer easyFileTriggerMQProducer) {
        return new RocketMQTriggerProducer(mqAsyncHandlerProperties, easyFileTriggerMQProducer);
    }

    @Bean
    @ConditionalOnMissingBean(RocketMQTriggerConsumer.class)
    public RocketMQTriggerConsumer rocketMQTriggerConsumer(RocketMqAsyncHandlerProperties mqAsyncHandlerProperties,
        EasyFileDownloadProperties downloadProperties,
        MQTriggerHandler mqTriggerHandler) {
        return new RocketMQTriggerConsumer(mqAsyncHandlerProperties, downloadProperties, mqTriggerHandler);
    }

    @Bean
    @ConditionalOnMissingBean(value = MQProducer.class, name = "easyFileTriggerMQProducer")
    public MQProducer easyFileTriggerMQProducer(RocketMqAsyncHandlerProperties mqAsyncHandlerProperties,
        EasyFileDownloadProperties easyFileDownloadProperties) {
        DefaultMQProducer producer = new DefaultMQProducer(mqAsyncHandlerProperties.getProduceGroup());
        producer.setNamesrvAddr(mqAsyncHandlerProperties.getHost());
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendAsyncFailed(mqAsyncHandlerProperties.getProduceTryTimes());
        producer.setSendLatencyFaultEnable(mqAsyncHandlerProperties.isProduceLatencyFaultEnable());
        String ipAddress = IpUtil.getIp();
        String[] split = ipAddress.split("\\.");
        producer.setInstanceName(
            Constants.EASY_FILE_SENDER + "@" + easyFileDownloadProperties.getAppId() + "@" + split[split.length - 1]
                + "@" + UtilAll.getPid());
        producer.setClientIP(ipAddress);
        try {
            producer.start();
        } catch (MQClientException ex) {
            log.error("EasyFileTriggerMQProducer producer start error", ex);
            ExceptionUtils.rethrow(ex);
        }
        return producer;
    }

}
