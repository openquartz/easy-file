package org.svnee.easyfile.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.UtilAll;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.IpUtil;
import org.svnee.easyfile.starter.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.impl.DefaultAsyncFileHandler;
import org.svnee.easyfile.starter.executor.impl.MqTriggerAsyncFileHandler;
import org.svnee.easyfile.starter.executor.impl.ScheduleTriggerAsyncFileHandler;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerHandler;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerProducer;
import org.svnee.easyfile.starter.executor.trigger.RocketMQTriggerConsumer;
import org.svnee.easyfile.starter.executor.trigger.RocketMQTriggerProducer;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties({DefaultAsyncHandlerThreadPoolProperties.class, ScheduleAsyncHandlerProperties.class,
    MqAsyncHandlerProperties.class})
public class AsyncFileHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileHandler.class)
    @ConditionalOnClass(MQProducer.class)
    @ConditionalOnProperty(prefix = MqAsyncHandlerProperties.PREFIX, name = "enable", havingValue = "true")
    public MqTriggerAsyncFileHandler mqTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        DownloadTriggerService downloadTriggerService,
        MQTriggerProducer mqTriggerProducer,
        MqAsyncHandlerProperties mqAsyncHandlerProperties) {
        return new MqTriggerAsyncFileHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            downloadTriggerService,
            mqAsyncHandlerProperties,
            mqTriggerProducer);
    }

    @Bean
    @ConditionalOnMissingBean(value = MQTriggerProducer.class, name = "easyFileTriggerMQProducer")
    @ConditionalOnClass(MQProducer.class)
    @ConditionalOnProperty(prefix = MqAsyncHandlerProperties.PREFIX, name = "enable", havingValue = "true")
    public MQTriggerProducer mqTriggerProducer(MqAsyncHandlerProperties mqAsyncHandlerProperties,
        MQProducer easyFileTriggerMQProducer) {
        return new RocketMQTriggerProducer(mqAsyncHandlerProperties, easyFileTriggerMQProducer);
    }

    @Bean
    @ConditionalOnMissingBean(RocketMQTriggerConsumer.class)
    @ConditionalOnClass(MQConsumer.class)
    @ConditionalOnProperty(prefix = MqAsyncHandlerProperties.PREFIX, name = "enable", havingValue = "true")
    public RocketMQTriggerConsumer rocketMQTriggerConsumer(MqAsyncHandlerProperties mqAsyncHandlerProperties,
        EasyFileDownloadProperties downloadProperties,
        MQTriggerHandler mqTriggerHandler) {
        return new RocketMQTriggerConsumer(mqAsyncHandlerProperties, downloadProperties, mqTriggerHandler);
    }

    @Bean
    @ConditionalOnMissingBean(MQProducer.class)
    @ConditionalOnClass(MQProducer.class)
    @ConditionalOnProperty(prefix = MqAsyncHandlerProperties.PREFIX, name = "enable", havingValue = "true")
    public MQProducer easyFileTriggerMQProducer(MqAsyncHandlerProperties mqAsyncHandlerProperties,
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

    @Bean
    @ConditionalOnMissingBean(BaseAsyncFileHandler.class)
    @ConditionalOnProperty(prefix = ScheduleAsyncHandlerProperties.PREFIX, name = "enable", havingValue = "true")
    public BaseAsyncFileHandler scheduleTriggerAsyncFileHandler(EasyFileDownloadProperties easyFileDownloadProperties,
        UploadService uploadService,
        DownloadStorageService downloadStorageService,
        DownloadTriggerService downloadTriggerService,
        BaseDefaultDownloadRejectExecutionHandler baseDefaultDownloadRejectExecutionHandler,
        ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties) {
        return new ScheduleTriggerAsyncFileHandler(easyFileDownloadProperties, uploadService, downloadStorageService,
            downloadTriggerService,
            scheduleAsyncHandlerProperties,
            baseDefaultDownloadRejectExecutionHandler);
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

}
