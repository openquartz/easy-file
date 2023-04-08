package com.openquartz.easyfile.storage.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 下载-错误码
 *
 * @author svnee
 */
public enum DownloadStorageErrorCode implements EasyFileErrorCode {

    DOWNLOAD_TASK_NOT_EXIST("01", "下载任务不存在"),
    DOWNLOAD_TASK_NOT_DOWNLOAD_SUCCESS("02","任务没有下载成功！"),
    ;

    private final String errorCode;
    private final String errorMsg;

    public static final String SIMPLE_BASE_CODE = "DownloadStorageError-";

    DownloadStorageErrorCode(String errorCode, String desc) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = desc;
    }

    @Override
    public final String getErrorCode() {
        return errorCode;
    }

    @Override
    public final String getErrorMsg() {
        return errorMsg;
    }
}
