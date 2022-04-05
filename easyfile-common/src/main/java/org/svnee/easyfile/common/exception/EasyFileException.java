package org.svnee.easyfile.common.exception;

import java.text.MessageFormat;

/**
 * 异步文件异常
 *
 * @author svnee
 */
public class EasyFileException extends RuntimeException {

    private final EasyFileErrorCode errorCode;

    public EasyFileException(EasyFileErrorCode errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public EasyFileException(EasyFileErrorCode errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public EasyFileErrorCode getErrorCode() {
        return errorCode;
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

}
