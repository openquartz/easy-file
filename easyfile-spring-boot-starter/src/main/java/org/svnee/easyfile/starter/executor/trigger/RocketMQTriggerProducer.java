package org.svnee.easyfile.starter.executor.trigger;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.starter.spring.boot.autoconfig.MqAsyncHandlerProperties;

/**
 * RocketMQ 消息发送方
 *
 * @author svnee
 **/
@Slf4j
public class RocketMQTriggerProducer implements MQTriggerProducer {

    private final MqAsyncHandlerProperties properties;
    private final MQProducer producer;

    public RocketMQTriggerProducer(MqAsyncHandlerProperties properties,
        MQProducer producer) {
        this.properties = properties;
        this.producer = producer;
    }

    @Override
    public boolean send(DownloadTriggerMessage triggerMessage) {
        String topic = properties.getTopic();
        Message message = new Message();
        message.setBody(JSONUtil.toJsonAsBytes(triggerMessage));
        message.setTopic(topic);
        SendResult result;
        try {
            result = producer.send(message, properties.getProduceTimeout());
        } catch (Exception ex) {
            log.error("[RocketMQTriggerProducer#send] send error!message:{}", message, ex);
            return false;
        }
        log.info("[RocketMQTriggerProducer#send]send result!message:{},result:{}", message, result);
        return true;
    }
}
