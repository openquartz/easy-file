package org.svnee.easyfile.server.notify;

import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.bean.Notifier;

/**
 * 飞书机器人通知
 *
 * @author svnee
 **/
@Component
public class FeiShuNotifySender implements NotifySender {

    @Override
    public NotifyWay notifyWay() {
        return NotifyWay.FEI_SHU;
    }

    @Override
    public boolean send(Notifier notifier, NotifyMessageTemplate messageTemplate) {
        return false;
    }
}
