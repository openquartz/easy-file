package com.openquartz.easyfile.common.request;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import lombok.Data;

/**
 * 导入回调请求
 *
 * @author svnee
 */
@Data
public class ImportCallbackRequest {

    /**
     * 注册ID
     */
    private Long registerId;

    /**
     * 状态
     */
    private UploadStatusEnum uploadStatus;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 失败文件地址
     */
    private String errorFileUrl;

    /**
     * 成功行数
     */
    private int successRows;

    /**
     * 失败行数
     */
    private int failRows;

    /**
     * 总行数
     */
    private int totalRows;
}
