package com.openquartz.easyfile.storage.local.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 导入-错误码
 *
 * @author svnee
 */
public enum ImportStorageErrorCode implements EasyFileErrorCode {

    IMPORT_TASK_NOT_EXIST("01", "导入任务不存在"),
    ;

    private final String errorCode;
    private final String errorMsg;

    public static final String SIMPLE_BASE_CODE = "ImportStorageError-";

    ImportStorageErrorCode(String errorCode, String desc) {
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
