package org.svnee.easyfile.server.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 异步下载异常码
 *
 * @author svnee
 **/
public enum AsyncDownloadExceptionCode implements EasyFileErrorCode {
    DOWNLOAD_TASK_IS_DISABLE("01", "下载任务已经被禁用"),
    ;

    private final String errorCode;
    private final String errorMsg;

    public static final String SIMPLE_BASE_CODE = "AsyncDownload-";

    AsyncDownloadExceptionCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }
}
