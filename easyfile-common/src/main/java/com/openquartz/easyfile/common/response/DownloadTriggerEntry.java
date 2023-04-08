package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class DownloadTriggerEntry {

    private Long registerId;

    private Integer triggerCount;

    public static DownloadTriggerEntry of(Long registerId, Integer triggerCount) {
        DownloadTriggerEntry triggerEntry = new DownloadTriggerEntry();
        triggerEntry.setTriggerCount(triggerCount);
        triggerEntry.setRegisterId(registerId);
        return triggerEntry;
    }
}
