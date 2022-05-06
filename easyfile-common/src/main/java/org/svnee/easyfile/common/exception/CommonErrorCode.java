package org.svnee.easyfile.common.exception;

import lombok.Getter;

/**
 * @author svnee
 **/
@Getter
public enum CommonErrorCode implements EasyFileErrorCode {

    PARAM_ILLEGAL_ERROR("01", "参数不合法异常"),

    ;
    private final String errorCode;
    private final String errorMsg;

    private static final String SIMPLE_BASE_CODE = "CommonError-";

    CommonErrorCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
    }
}
