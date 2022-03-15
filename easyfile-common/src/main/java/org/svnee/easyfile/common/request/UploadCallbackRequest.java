package org.svnee.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.svnee.easyfile.common.dictionary.UploadFileSystem;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;

/**
 * 上传回传结果请求
 *
 * @author xuzhao
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
     * 上传系统枚举
     */
    @NotNull(message = "上传文件系统不能为空")
    private UploadFileSystem system;

    /**
     * 上传文件状态
     */
    @NotNull(message = "上传状态不能为空")
    private UploadStatusEnum uploadStatus;

    /**
     * 异常信息备注
     */
    private String errorMsg;

}
