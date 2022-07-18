package org.svnee.easyfile.starter.executor.trigger;

/**
 * 消息触发器生产器
 *
 * @author svnee
 */
public interface MQTriggerProducer {

    /**
     * 发送消息
     *
     * @param triggerMessage 触发消息
     */
    void send(DownloadTriggerMessage triggerMessage);
}
