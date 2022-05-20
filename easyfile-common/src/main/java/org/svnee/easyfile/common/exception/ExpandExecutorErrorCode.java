package org.svnee.easyfile.common.exception;

import lombok.Getter;

/**
 * @author svnee
 **/
@Getter
public enum ExpandExecutorErrorCode implements EasyFileErrorCode {

    LIMITING_STRATEGY_EXECUTOR_EXIST_ERROR("01", "策略:{0} 限流执行器已经存在！", true),
    LIMITING_STRATEGY_EXECUTOR_NOT_EXIST_ERROR("02", "策略:{0} 限流执行器不存在！", true),
    ;

    public static final String SIMPLE_BASE_CODE = "ExpandExecutorError-";

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    ExpandExecutorErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    ExpandExecutorErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
