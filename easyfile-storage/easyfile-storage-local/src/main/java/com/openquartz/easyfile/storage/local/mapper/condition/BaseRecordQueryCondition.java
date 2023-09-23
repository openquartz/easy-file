package com.openquartz.easyfile.storage.local.mapper.condition;

import java.util.List;
import lombok.Data;

import java.util.Date;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;

/**
 * 查询
 *
 * @author svnee
 */
@Data
public class BaseRecordQueryCondition {

    /**
     * 限定App ID
     * 非必填
     */
    private List<String> limitedAppIdList;

    /**
     * 下载编码
     * 选填
     */
    private String downloadCode;

    /**
     * 下载人
     */
    private String downloadOperateBy;

    /**
     * 开始查询时间
     */
    private Date startCreateTime;

    /**
     * 结束查询时间
     */
    private Date endCreateTime;

    /**
     * 下载状态
     * 非必须
     */
    private HandleStatusEnum uploadStatus;

    /**
     * 过期时间
     * 非必须
     */
    private Date maxInvalidTime;

    /**
     * 最大的限制条数
     * 非必须
     * key: startIndex
     * value: offset
     */
    private Pair<Integer, Integer> startOffset;


}
