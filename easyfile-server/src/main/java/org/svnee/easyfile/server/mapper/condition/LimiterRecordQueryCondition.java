package org.svnee.easyfile.server.mapper.condition;

import lombok.Data;

import java.util.Date;

/**
 * 查询
 *
 * @author svnee
 */
@Data
public class LimiterRecordQueryCondition {

    /**
     * 必填
     */
    private String appId;

    /**
     * 下载编码
     * 选填
     */
    private String downloadCode;

    /**
     * 最小创建时间
     */
    private Date minCreateTime;

}
