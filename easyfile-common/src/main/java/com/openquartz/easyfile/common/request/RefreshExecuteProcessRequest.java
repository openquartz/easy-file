package com.openquartz.easyfile.common.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import lombok.Data;
import com.openquartz.easyfile.common.serdes.BaseEnumDeserializer;
import com.openquartz.easyfile.common.serdes.BaseEnumSerializer;

/**
 * 刷新执行进度请求
 *
 * @author svnee
 **/
@Data
public class RefreshExecuteProcessRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 执行进度
     */
    private Integer executeProcess;

    /**
     * 下一个上传状态
     */
    @JsonSerialize(using = BaseEnumSerializer.class)
    @JsonDeserialize(using = BaseEnumDeserializer.class)
    private UploadStatusEnum nextUploadStatus;

}
