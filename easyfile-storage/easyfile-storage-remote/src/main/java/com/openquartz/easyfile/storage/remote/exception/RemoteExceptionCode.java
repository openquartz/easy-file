package com.openquartz.easyfile.storage.remote.exception;

import lombok.Getter;
import com.openquartz.easyfile.common.exception.EasyFileErrorCode;

/**
 * 远程调用异常Code
 *
 * @author svnee
 **/
@Getter
public enum RemoteExceptionCode implements EasyFileErrorCode {
    SHUTDOWN_ERROR("01", "ShutdownError"),
    HTTP_ERROR("02","HttpError"),
    HTTP_RESPONSE_BODY_NULL_ERROR("03","Http ResponseBody Null Error!"),
    ;

    private final String errorCode;
    private final String errorMsg;

    private static final String SIMPLE_BASE_COE = "RemoteException-";

    RemoteExceptionCode(String errorCode, String errorMsg) {
        this.errorCode = SIMPLE_BASE_COE + errorCode;
        this.errorMsg = errorMsg;
    }
}
