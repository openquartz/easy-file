package org.svnee.easyfile.starter.executor.trigger;

import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
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
    public void send(DownloadTriggerMessage triggerMessage) {
        String topic = properties.getTopic();
        Message message = new Message();
        message.setBody(JSONUtil.toJsonAsBytes(triggerMessage));
        message.setTopic(topic);
        SendResult result = null;
        try {
            result = producer.send(message, properties.getSendTimeout());
        } catch (Exception ex) {
            ExceptionUtils.rethrow(ex);
        }
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(
                MessageFormat.format("rocket send error,sendStatus:{0},msgId:{1},topic:{2}",
                    result.getSendStatus(),
                    result.getMsgId(), topic));
        }
    }
}
