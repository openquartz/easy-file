package org.svnee.easyfile.starter.exception;

/**
 * 异步文件异常
 *
 * @author svnee
 */
public class EasyFileException extends RuntimeException {

    private EasyFileErrorCode errorCode;

    public EasyFileException(EasyFileErrorCode errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }
}
