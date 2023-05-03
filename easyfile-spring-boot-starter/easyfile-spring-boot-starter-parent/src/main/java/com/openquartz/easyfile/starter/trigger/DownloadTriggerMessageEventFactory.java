package com.openquartz.easyfile.starter.trigger;

import com.lmax.disruptor.EventFactory;
import com.openquartz.easyfile.core.executor.trigger.DownloadTriggerMessage;

/**
 * event factory
 *
 * @author svnee
 **/
public class DownloadTriggerMessageEventFactory implements EventFactory<DownloadTriggerMessage> {

    @Override
    public DownloadTriggerMessage newInstance() {
        return new DownloadTriggerMessage();
    }
}
