package com.openquartz.easyfile.core.exception;

/**
 * 并发导入异常
 *
 * @author svnee
 */
public class ConcurrentImportException extends RuntimeException {

    public ConcurrentImportException(String message) {
        super(message);
    }

    public ConcurrentImportException(String message, Throwable cause) {
        super(message, cause);
    }
}