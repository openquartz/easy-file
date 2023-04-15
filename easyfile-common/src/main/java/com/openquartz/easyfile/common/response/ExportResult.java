package com.openquartz.easyfile.common.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import lombok.Data;
import com.openquartz.easyfile.common.serdes.BaseEnumDeserializer;
import com.openquartz.easyfile.common.serdes.BaseEnumSerializer;

/**
 * 导出结果
 *
 * @author svnee
 */
@Data
public class ExportResult {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 上传状态
     */
    @JsonSerialize(using = BaseEnumSerializer.class)
    @JsonDeserialize(using = BaseEnumDeserializer.class)
    private UploadStatusEnum uploadStatus;

    /**
     * 上传文件系统
     */
    private String fileSystem;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件名
     *
     * @since 1.2.1
     */
    private String fileName;

    /**
     * 异常Msg
     */
    private String errorMsg;

}
