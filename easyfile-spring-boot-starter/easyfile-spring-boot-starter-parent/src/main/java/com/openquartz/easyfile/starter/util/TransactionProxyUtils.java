package com.openquartz.easyfile.starter.util;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author svnee
 */
public class TransactionProxyUtils {

    private TransactionProxyUtils() {
    }

    public static void doAfterCommit(Runnable runnable) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()){
            runnable.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
               runnable.run();
            }
        });
    }
}
