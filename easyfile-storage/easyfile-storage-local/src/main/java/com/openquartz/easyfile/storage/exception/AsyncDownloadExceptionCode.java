package com.openquartz.easyfile.storage.exception;

import lombok.Getter;
import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 异步下载异常码
 *
 * @author svnee
 **/
@Getter
public enum AsyncDownloadExceptionCode implements EasyFileErrorCode {
    DOWNLOAD_TASK_IS_DISABLE("01", "下载任务已经被禁用"),
    DOWNLOAD_TASK_NOT_EXIST("02", "下载任务不存在"),
    DOWNLOAD_RECORD_NOT_EXIST("03", "下载记录不存在"),
    DOWNLOAD_STATUS_NOT_SUPPORT("04", "下载状态不支持"),
    ;

    private final String errorCode;
    private final String errorMsg;

    public static final String SIMPLE_BASE_CODE = "AsyncDownload-";

    AsyncDownloadExceptionCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
    }
}
