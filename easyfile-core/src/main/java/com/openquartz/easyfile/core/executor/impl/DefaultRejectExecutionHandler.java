package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.exception.ExportErrorCode;
import com.openquartz.easyfile.core.exception.ExportRejectExecuteException;
import com.openquartz.easyfile.core.executor.BaseDefaultRejectExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author svnee
 * 默认下载执行器时间
 * 如果需要进行其他业务逻辑也可自行实现,比如：报警通知等等
 **/
@Slf4j
public class DefaultRejectExecutionHandler implements BaseDefaultRejectExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.warn("[DefaultDownloadRejectExecutionHandler] reject execution,r:{}", r.toString());
        throw new ExportRejectExecuteException(ExportErrorCode.DOWNLOAD_OVER_WAIT_NUM_REJECT);
    }

}
