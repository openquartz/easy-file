package org.svnee.easyfile.common.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.serdes.BaseEnumDeserializer;
import org.svnee.easyfile.common.serdes.BaseEnumSerializer;

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
     * 异常Msg
     */
    private String errorMsg;

}
