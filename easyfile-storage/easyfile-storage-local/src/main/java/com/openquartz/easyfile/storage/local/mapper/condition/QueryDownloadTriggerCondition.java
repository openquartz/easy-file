package com.openquartz.easyfile.storage.local.mapper.condition;

import com.openquartz.easyfile.storage.local.dictionary.DownloadTriggerStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 查询下载触发器条件
 *
 * @author svnee
 **/
@Data
public class QueryDownloadTriggerCondition {

    /**
     * 触发状态
     */
    private List<DownloadTriggerStatusEnum> triggerStatusList;

    /**
     * 创建者
     */
    private String creatingOwner;

    /**
     * 最后执行时间范围-开始时间
     */
    private LocalDateTime lastExecuteStartTime;

    /**
     * 最后执行时间范围-结束时间
     */
    private LocalDateTime lastExecuteEndTime;

    /**
     * 最大触发次数
     */
    private Integer maxTriggerCount;

    /**
     * 最小触发次数
     */
    private Integer minTriggerCount;

    /**
     * 一次请求多少量
     */
    private Integer offset;
}
