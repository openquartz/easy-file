package org.svnee.easyfile.common.response;

import lombok.Data;
import org.svnee.easyfile.common.dictionary.UploadFileSystem;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;

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
    private UploadStatusEnum uploadStatus;

    /**
     * 上传文件系统
     */
    private UploadFileSystem fileSystem;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 异常Msg
     */
    private String errorMsg;

}
