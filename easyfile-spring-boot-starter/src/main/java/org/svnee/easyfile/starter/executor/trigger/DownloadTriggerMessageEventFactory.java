package org.svnee.easyfile.starter.executor.trigger;

import com.lmax.disruptor.EventFactory;

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
