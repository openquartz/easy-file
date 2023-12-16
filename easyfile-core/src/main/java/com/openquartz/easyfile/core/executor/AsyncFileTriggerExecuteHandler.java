package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.IRequest;

/**
 * @author svnee
 */
public interface AsyncFileTriggerExecuteHandler {

    /**
     * executor executes file handler
     *
     * @param executor    executor
     * @param baseRequest base request
     * @param registerId  register id
     */
    <T extends Executor, R extends IRequest> void execute(T executor, R baseRequest, Long registerId);

}
