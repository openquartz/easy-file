package com.openquartz.easyfile.core.exception;

/**
 * 文件解析异常
 *
 * @author svnee
 */
public class FileParseException extends RuntimeException {

    public FileParseException(String message) {
        super(message);
    }

    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}