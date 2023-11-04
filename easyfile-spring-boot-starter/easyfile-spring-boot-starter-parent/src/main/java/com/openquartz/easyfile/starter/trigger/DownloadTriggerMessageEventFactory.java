package com.openquartz.easyfile.starter.trigger;

import com.lmax.disruptor.EventFactory;
import com.openquartz.easyfile.core.executor.trigger.TriggerMessage;

/**
 * event factory
 *
 * @author svnee
 **/
public class DownloadTriggerMessageEventFactory implements EventFactory<TriggerMessage> {

    @Override
    public TriggerMessage newInstance() {
        return new TriggerMessage();
    }
}
