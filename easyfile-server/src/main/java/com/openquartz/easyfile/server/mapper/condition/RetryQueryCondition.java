package com.openquartz.easyfile.server.mapper.condition;

import lombok.Data;

import java.util.Date;
import java.util.List;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;

/**
 * 重试查询条件
 *
 * @author svnee
 */
@Data
public class RetryQueryCondition {

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 限制行数
     */
    private Integer limit;

    /**
     * 需重试状态
     */
    private List<UploadStatusEnum> needRetryableStatusList;

    private RetryQueryCondition(Date startTime, Date endTime,
        List<UploadStatusEnum> needRetryableStatusList, Integer limit) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.limit = limit;
        this.needRetryableStatusList = needRetryableStatusList;
    }

    public static RetryQueryCondition create(Date startTime, Date endTime,
        List<UploadStatusEnum> needRetryableStatusList, Integer limit) {
        return new RetryQueryCondition(startTime, endTime, needRetryableStatusList, limit);
    }

}
