package org.svnee.easyfile.core.executor.trigger;

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
     * @return 是否发送成功
     */
    boolean send(DownloadTriggerMessage triggerMessage);
}
