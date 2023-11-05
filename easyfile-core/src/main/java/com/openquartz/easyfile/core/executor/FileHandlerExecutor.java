package com.openquartz.easyfile.core.executor;

/**
 * file handler executor
 *
 * @param <T> executor type
 * @param <R> request type
 */
public interface FileHandlerExecutor<T, R> {

    /**
     * executor executes file handler
     *
     * @param executor    executor
     * @param baseRequest base request
     * @param registerId  register id
     */
    void execute(T executor, R baseRequest, Long registerId);

}
