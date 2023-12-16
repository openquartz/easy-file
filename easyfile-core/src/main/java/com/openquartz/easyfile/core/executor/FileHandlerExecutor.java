package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.IRequest;

/**
 * file handler executor
 *
 * @param <T> executor type
 * @param <R> request type
 */

public interface FileHandlerExecutor<T extends Executor, R extends IRequest> {

    /**
     * executor executes file handler
     *
     * @param executor    executor
     * @param baseRequest base request
     * @param registerId  register id
     */
    void execute(T executor, R baseRequest, Long registerId);

}
