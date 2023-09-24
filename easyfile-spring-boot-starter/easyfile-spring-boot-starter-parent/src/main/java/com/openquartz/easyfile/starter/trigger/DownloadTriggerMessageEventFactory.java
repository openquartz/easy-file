package com.openquartz.easyfile.starter.trigger;

import com.lmax.disruptor.EventFactory;
import com.openquartz.easyfile.core.executor.trigger.ExportTriggerMessage;

/**
 * event factory
 *
 * @author svnee
 **/
public class DownloadTriggerMessageEventFactory implements EventFactory<ExportTriggerMessage> {

    @Override
    public ExportTriggerMessage newInstance() {
        return new ExportTriggerMessage();
    }
}
