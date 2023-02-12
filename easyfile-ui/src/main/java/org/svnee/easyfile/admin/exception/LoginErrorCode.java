package org.svnee.easyfile.admin.exception;

import lombok.Getter;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * login error code
 * @author svnee
 */
@Getter
public enum LoginErrorCode implements EasyFileErrorCode {

    AUTH_FAILED_ERROR("01", "Auth Failed!"),
    USER_NOT_EXISTED_ERROR("02", "User Not Existed!"),
    LOGIN_PARAM_EMPTY_ERROR("03","login param is empty"),
    LOGIN_PARAM_INVALID_ERROR("04","login param invalid"),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String BASE_SIMPLE_CODE = "LoginError-";

    LoginErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    LoginErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = BASE_SIMPLE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
