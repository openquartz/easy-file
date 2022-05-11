package org.svnee.easyfile.server.notify;

import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.bean.Notifier;

/**
 * 邮箱通知
 *
 * @author svnee
 **/
@Component
public class EmailNotifySender implements NotifySender {

    @Override
    public NotifyWay notifyWay() {
        return NotifyWay.EMAIL;
    }

    @Override
    public boolean send(Notifier notifier, NotifyMessageTemplate messageTemplate) {
        return false;
    }
}
