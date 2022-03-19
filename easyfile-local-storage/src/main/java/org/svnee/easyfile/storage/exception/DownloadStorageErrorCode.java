package org.svnee.easyfile.storage.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 下载-错误码
 *
 * @author svnee
 */
public enum DownloadStorageErrorCode implements EasyFileErrorCode {

    DOWNLOAD_TASK_NOT_EXIST("01", "下载任务不存在"),
    ;

    private final String errorCode;
    private final String desc;

    public static final String SIMPLE_BASE_CODE = "DownloadStorageError-";

    DownloadStorageErrorCode(String errorCode, String desc) {
        this.errorCode = errorCode;
        this.desc = desc;
    }

    @Override
    public final String getErrorCode() {
        return null;
    }

    @Override
    public final String getErrorMsg() {
        return null;
    }
}
