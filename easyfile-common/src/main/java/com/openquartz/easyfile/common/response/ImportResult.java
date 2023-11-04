package com.openquartz.easyfile.common.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.serdes.BaseEnumDeserializer;
import com.openquartz.easyfile.common.serdes.BaseEnumSerializer;
import lombok.Data;

/**
 * 导入结果
 * @author svnee
 */
@Data
public class ImportResult {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 处理状态
     */
    @JsonSerialize(using = BaseEnumSerializer.class)
    @JsonDeserialize(using = BaseEnumDeserializer.class)
    private HandleStatusEnum handleStatus;

    /**
     * 异常Msg
     */
    private String errorMsg;

}
