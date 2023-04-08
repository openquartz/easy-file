package com.openquartz.easyfile.common.exception;

/**
 * LogConsumer
 *
 * @author svnee
 **/
@FunctionalInterface
public interface LogCallback {

    /**
     * log
     */
    void log();
}
