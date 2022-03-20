package org.svnee.easyfile.server.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.svnee.easyfile.common.bean.Notifier;
import org.svnee.easyfile.server.notify.NotifyMessageTemplate;
import org.svnee.easyfile.server.service.NotifyService;

/**
 * 通知服务
 *
 * @author svnee
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    @Override
    public void notify(Notifier notifier, NotifyMessageTemplate template) {
        // TODO: 2020/8/25 待处理
        log.info("---------------------------消息通知：通知人:{},消息:{}", notifier, template);
    }

}
