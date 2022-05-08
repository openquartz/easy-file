package org.svnee.easyfile.server.exception;

import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * @author svnee
 **/
public enum LoginExceptionCode implements EasyFileErrorCode {
    LOGIN_EXPIRE_ERROR("01", "登录时间过长，请退出重新登录"),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "Login-";

    LoginExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    LoginExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
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
