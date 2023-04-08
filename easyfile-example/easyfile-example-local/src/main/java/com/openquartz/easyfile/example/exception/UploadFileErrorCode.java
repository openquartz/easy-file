package com.openquartz.easyfile.example.exception;

import lombok.Getter;
import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

@Getter
public enum UploadFileErrorCode implements EasyFileErrorCode {

    FILE_IDENTIFY_KEY_IS_NOT_EXIST("01", "文件IdentifyKey不存在"),
    ;

    private final String errorCode;
    private final String errorMsg;

    private static final String BASE_KEY = "UploadFileError-";

    UploadFileErrorCode(String errorCode, String errorMsg) {
        this.errorCode = BASE_KEY + errorCode;
        this.errorMsg = errorMsg;
    }
}
