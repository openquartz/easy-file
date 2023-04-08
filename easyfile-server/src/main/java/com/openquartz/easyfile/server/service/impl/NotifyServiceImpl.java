package com.openquartz.easyfile.server.service.impl;

import static com.openquartz.easyfile.server.exception.NotifyExceptionCode.NOTIFY_SENDER_REPEAT_ERROR;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.server.notify.NotifyConfig;
import com.openquartz.easyfile.server.notify.NotifyMessageTemplate;
import com.openquartz.easyfile.server.notify.NotifySender;
import com.openquartz.easyfile.server.notify.NotifyWay;
import com.openquartz.easyfile.server.service.NotifyService;

/**
 * 通知服务
 *
 * @author svnee
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService, BeanPostProcessor {

    private final NotifyConfig notifyConfig;
    private final Map<NotifyWay, NotifySender> notifySenderMap = new ConcurrentHashMap<>(NotifyWay.values().length);

    @Override
    public void notify(Notifier notifier, NotifyMessageTemplate template) {
        log.info("开始消息通知：通知人:{},消息:{}", notifier, template);
        if (!notifyConfig.isEnabled()) {
            return;
        }
        for (NotifyWay way : notifyConfig.getWays()) {
            NotifySender sender = notifySenderMap.get(way);
            sender.send(notifier, template);
        }

        log.info("开始消息通知：通知人:{},消息:{}", notifier, template);
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof NotifySender) {
            NotifySender sender = (NotifySender) bean;
            Asserts.isTrue(!notifySenderMap.containsKey(sender.notifyWay()), NOTIFY_SENDER_REPEAT_ERROR,
                sender.notifyWay().getCode());
            notifySenderMap.put(sender.notifyWay(), sender);
        }
        return bean;
    }
}
