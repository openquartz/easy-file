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

    LoginExceptionCode(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
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
