package org.svnee.easyfile.starter.exception;

/**
 * @author svnee
 * @desc 文件生成异常
 **/
public class GenerateFileException extends EasyFileException {

    private GenerateFileErrorCode errorCode;

    public GenerateFileException(GenerateFileErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }


}
