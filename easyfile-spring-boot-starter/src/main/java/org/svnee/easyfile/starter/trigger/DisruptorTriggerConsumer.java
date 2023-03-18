package org.svnee.easyfile.starter.trigger;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.core.executor.trigger.DownloadTriggerMessage;
import org.svnee.easyfile.core.executor.trigger.MQTriggerHandler;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;

/**
 * Disruptor-消费
 *
 * @author svnee
 **/
@Slf4j
public class DisruptorTriggerConsumer implements EventHandler<DownloadTriggerMessage> {

    private final EasyFileDownloadProperties downloadProperties;
    private final MQTriggerHandler mqTriggerHandler;

    public DisruptorTriggerConsumer(EasyFileDownloadProperties downloadProperties,
        MQTriggerHandler mqTriggerHandler) {
        this.downloadProperties = downloadProperties;
        this.mqTriggerHandler = mqTriggerHandler;
    }

    @Override
    public void onEvent(DownloadTriggerMessage triggerMessage, long l, boolean b) {
        log.info("[DisruptorTriggerConsumer#onEvent] message:{}", triggerMessage);
        try {
            mqTriggerHandler.handle(triggerMessage);
        } catch (Exception ex) {
            log.error("[DisruptorTriggerConsumer#onEvent]Consumption failure,message:{},properties:{}", triggerMessage,
                downloadProperties, ex);
            throw ex;
        }
        log.info("[DisruptorTriggerConsumer#onEvent]Consumption Complete,magId:{}", l);
    }
}
