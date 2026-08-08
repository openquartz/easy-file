package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * 导入触发器认领条目
 *
 * @author svnee
 */
@Data
public class ImportTriggerEntry {

    private Long registerId;

    private Integer triggerCount;

    public static ImportTriggerEntry of(Long registerId, Integer triggerCount) {
        ImportTriggerEntry triggerEntry = new ImportTriggerEntry();
        triggerEntry.setTriggerCount(triggerCount);
        triggerEntry.setRegisterId(registerId);
        return triggerEntry;
    }
}
