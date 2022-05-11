package org.svnee.easyfile.server.exception;

import lombok.Getter;
import org.svnee.easyfile.common.exception.EasyFileErrorCode;

/**
 * 通知异常code
 *
 * @author svnee
 **/
@Getter
public enum NotifyExceptionCode implements EasyFileErrorCode {

    NOTIFY_SENDER_REPEAT_ERROR("01", "通知方式:{0}已经存在！", true),

    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "NotifyError-";

    NotifyExceptionCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    NotifyExceptionCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
