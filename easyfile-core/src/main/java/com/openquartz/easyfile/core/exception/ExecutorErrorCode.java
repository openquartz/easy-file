package com.openquartz.easyfile.core.exception;

import com.openquartz.easyfile.common.exception.EasyFileErrorCode;
import lombok.Getter;

@Getter
public enum ExecutorErrorCode implements EasyFileErrorCode {

    EXECUTOR_DUPLICATE("01","Executor is duplicate!"),

    EXECUTOR_NOT_EXIST("02","Executor not exist!"),

    ;
    /** 异常码 */
    private final String errorCode;
    /** 异常信息 */
    private final String errorMsg;
    private final boolean replacePlaceHold;

    /** 基础码前缀 */
    private static final String SIMPLE_BASE_PREFIX = "Executor-";

    ExecutorErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    ExecutorErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_PREFIX + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
