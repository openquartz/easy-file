package org.svnee.easyfile.server.notify;

import org.svnee.easyfile.common.bean.Notifier;

/**
 * 通知发送者
 *
 * @author svnee
 **/
public interface NotifySender {

    /**
     * 通知方式
     *
     * @return 通知方式
     */
    NotifyWay notifyWay();

    /**
     * 发送消息
     *
     * @param notifier 通知人
     * @param messageTemplate 通知消息模板
     * @return 发送是否成功
     */
    boolean send(Notifier notifier, NotifyMessageTemplate messageTemplate);

}
