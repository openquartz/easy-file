package org.svnee.easyfile.starter.executor.trigger;

/**
 * MQTriggerConsumer
 *
 * @author svnee
 */
public interface MQTriggerHandler {

    /**
     * 处理消息
     *
     * @param message 消息
     */
    void handle(DownloadTriggerMessage message);

}
