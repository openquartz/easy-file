package com.openquartz.easyfile.server.common.lock;

/**
 * consumer function
 *
 * @author svnee
 */
@FunctionalInterface
public interface Consumer {

    /**
     * consume
     */
    void consume();
}
