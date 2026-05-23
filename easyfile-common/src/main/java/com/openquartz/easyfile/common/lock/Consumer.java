package com.openquartz.easyfile.common.lock;

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
