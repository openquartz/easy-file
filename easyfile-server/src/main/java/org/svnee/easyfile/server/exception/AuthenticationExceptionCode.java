package org.svnee.easyfile.server.exception;

import lombok.Getter;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 认证异常码
 *
 * @author svnee
 **/
@Getter
public enum AuthenticationExceptionCode implements EasyFileErrorCode {

    AUTH_FAILED_ERROR("01", "Auth Failed!"),
    USER_NOT_EXISTED_ERROR("02", "User Not Existed!"),
    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String BASE_SIMPLE_CODE = "AuthError-";

    AuthenticationExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    AuthenticationExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = BASE_SIMPLE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
