package com.openquartz.easyfile.common.response;

import lombok.Data;

/**
 * 导入触发器Entry
 *
 * @author svnee
 */
@Data
public class ImportTriggerEntry {

    /**
     * registerId
     */
    private Long registerId;

    /**
     * triggerCount
     */
    private Integer triggerCount;

}
