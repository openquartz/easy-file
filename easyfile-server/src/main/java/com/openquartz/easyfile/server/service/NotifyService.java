package com.openquartz.easyfile.server.service;

import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.server.notify.NotifyMessageTemplate;

/**
 * 通知服务
 *
 * @author svnee
 */
public interface NotifyService {

    /**
     * 通知服务
     *
     * @param notifier 通知人
     * @param template 通知消息模版
     */
    void notify(Notifier notifier, NotifyMessageTemplate template);

}
