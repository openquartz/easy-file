package com.openquartz.easyfile.storage.local.mapper.condition;

import com.openquartz.easyfile.storage.local.dictionary.ImportTriggerStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 查询导入触发器条件
 *
 * @author svnee
 */
@Data
public class QueryImportTriggerCondition {

    private List<ImportTriggerStatusEnum> triggerStatusList;

    private String creatingOwner;

    private LocalDateTime lastExecuteStartTime;

    private LocalDateTime lastExecuteEndTime;

    private Integer maxTriggerCount;

    private Integer minTriggerCount;

    private Integer offset;
}
