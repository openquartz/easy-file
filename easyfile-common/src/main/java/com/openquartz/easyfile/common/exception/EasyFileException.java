package com.openquartz.easyfile.common.exception;

import lombok.Getter;

import java.text.MessageFormat;

/**
 * 异步文件异常
 *
 * @author svnee
 */
@Getter
public class EasyFileException extends RuntimeException {

    private final transient EasyFileErrorCode errorCode;

    public EasyFileException(EasyFileErrorCode errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public EasyFileException(EasyFileErrorCode errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    /**
     * 替换占位符号
     *
     * @param placeHold 占位
     * @return 异常
     */
    public static EasyFileException replacePlaceHold(EasyFileErrorCode errorCode, Object... placeHold) {
        throw new EasyFileException(errorCode, MessageFormat.format(errorCode.getErrorMsg(), placeHold));
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
