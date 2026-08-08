package com.openquartz.easyfile.storage.local.mapper.condition;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 导入记录查询条件
 *
 * @author svnee
 */
@Data
public class ImportRecordQueryCondition {

    /**
     * 限定 App ID
     */
    private List<String> limitedAppIdList;

    /**
     * 导入编码
     */
    private String importCode;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 业务幂等键
     */
    private String businessUniqueKey;

    /**
     * 开始查询时间
     */
    private Date startCreateTime;

    /**
     * 结束查询时间
     */
    private Date endCreateTime;

    /**
     * 导入状态
     */
    private ImportStatusEnum importStatus;

    /**
     * 分页
     * key: startIndex
     * value: offset
     */
    private Pair<Integer, Integer> startOffset;

}
