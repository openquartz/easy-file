package org.svnee.easyfile.starter.executor.trigger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyfile.common.util.IpUtil;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.RocketMqAsyncHandlerProperties;

/**
 * RocketMQ 触发消费者
 *
 * @author svnee
 **/
@Slf4j
public class RocketMQTriggerConsumer implements InitializingBean {

    private final RocketMqAsyncHandlerProperties mqAsyncHandlerProperties;
    private final EasyFileDownloadProperties downloadProperties;
    private final MQTriggerHandler mqTriggerHandler;

    public RocketMQTriggerConsumer(RocketMqAsyncHandlerProperties mqAsyncHandlerProperties,
        EasyFileDownloadProperties downloadProperties,
        MQTriggerHandler mqTriggerHandler) {
        this.mqAsyncHandlerProperties = mqAsyncHandlerProperties;
        this.downloadProperties = downloadProperties;
        this.mqTriggerHandler = mqTriggerHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MQPushConsumer consumer = create(mqAsyncHandlerProperties);
        consumer.start();
    }

    public MQPushConsumer create(RocketMqAsyncHandlerProperties properties) {
        DefaultMQPushConsumer consumer = null;
        log.info("[RocketMQTriggerConsumer#create],properties:{}", properties);
        try {
            consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
            consumer.setMaxReconsumeTimes(1);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setConsumeThreadMin(properties.getConsumerMinThread());
            consumer.setConsumeThreadMax(properties.getConsumerMaxThread() + 1);
            consumer.setNamesrvAddr(properties.getHost());
            consumer.subscribe(properties.getTopic(), "*");

            String ipAddress = IpUtil.getIp();
            String[] split = ipAddress.split("\\.");

            consumer.setClientIP(ipAddress);
            consumer.setInstanceName(
                downloadProperties.getAppId() + "@" + UtilAll.getPid() + "@" + split[split.length - 1]);
            //集群消费
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.setConsumeConcurrentlyMaxSpan(properties.getConsumeConcurrentlyMaxSpan());
            // 开启内部类实现监听
            consumer.registerMessageListener((MessageListenerConcurrently) (messageExtList, context) -> {
                if (CollectionUtils.isEmpty(messageExtList)) {
                    log.warn("[RocketMQTriggerConsumer#create]Message List is empty,properties:{}", properties);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                MessageExt message = messageExtList.get(0);
                try {
                    mqTriggerHandler.handle(JSONUtil.parseObject(message.getBody(), DownloadTriggerMessage.class));
                } catch (Throwable ex) {
                    log.error("[RocketMQTriggerConsumer#create]Consumption failure,message:{},properties:{}", message,
                        properties, ex);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                log.info("[RocketMQTriggerConsumer#create]Consumption Complete,rocketMsgId:{}", message.getMsgId());
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        } catch (MQClientException ex) {
            log.error("[RocketMQTriggerConsumer#create] create and start error,properties:{}", properties, ex);
            ExceptionUtils.rethrow(ex);
        }
        log.info("[RocketMQTriggerConsumer#create] create and start end,properties:{}", properties);
        return consumer;
    }

}
