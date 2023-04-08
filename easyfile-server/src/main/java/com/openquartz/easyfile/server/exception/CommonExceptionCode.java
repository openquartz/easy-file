package com.openquartz.easyfile.server.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 通用异常码
 *
 * @author svnee
 **/
public enum CommonExceptionCode implements EasyFileErrorCode {
    ILLEGAL_PARAM_ERROR("01", "参数不合法:{0}", true),
    DEFAULT_ERROR("02", "默认异常"),

    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "Common-";

    CommonExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    CommonExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
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
