package com.openquartz.easyfile.core.exception;

import com.openquartz.easyfile.common.exception.EasyFileException;

/**
 * 文件生成异常
 *
 * @author svnee
 **/
public class GenerateFileException extends EasyFileException {

    private final GenerateFileErrorCode errorCode;

    public GenerateFileException(GenerateFileErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    @Override
    public GenerateFileErrorCode getErrorCode() {
        return errorCode;
    }
}
