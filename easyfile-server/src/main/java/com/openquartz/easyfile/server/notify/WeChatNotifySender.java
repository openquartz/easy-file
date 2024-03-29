package com.openquartz.easyfile.server.notify;

import org.springframework.stereotype.Component;
import com.openquartz.easyfile.common.bean.Notifier;

/**
 * WeChat 通知发送者
 *
 * @author svnee
 **/
@Component
public class WeChatNotifySender implements NotifySender {

    @Override
    public NotifyWay notifyWay() {
        return NotifyWay.WECHAT;
    }

    @Override
    public boolean send(Notifier notifier, NotifyMessageTemplate messageTemplate) {
        return false;
    }
}
