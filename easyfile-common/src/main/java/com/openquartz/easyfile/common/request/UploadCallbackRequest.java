package com.openquartz.easyfile.common.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import javax.validation.constraints.NotNull;
import lombok.Data;
import com.openquartz.easyfile.common.serdes.BaseEnumDeserializer;
import com.openquartz.easyfile.common.serdes.BaseEnumSerializer;

/**
 * 上传回传结果请求
 *
 * @author svnee
 */
@Data
public class UploadCallbackRequest {

    /**
     * 注册下载ID
     */
    @NotNull(message = "注册ID不能为空")
    private Long registerId;

    /**
     * 下载URL链接
     */
    private String fileUrl;

    /**
     * fileName
     *
     * @since 1.2.1
     */
    private String fileName;

    /**
     * 上传系统枚举
     */
    @NotNull(message = "上传文件系统不能为空")
    private String system;

    /**
     * 上传文件状态
     */
    @NotNull(message = "上传状态不能为空")
    @JsonSerialize(using = BaseEnumSerializer.class)
    @JsonDeserialize(using = BaseEnumDeserializer.class)
    private UploadStatusEnum uploadStatus;

    /**
     * 异常信息备注
     */
    private String errorMsg;

}
