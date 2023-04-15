package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.core.exception.DownloadErrorCode;
import com.openquartz.easyfile.core.exception.DownloadRejectExecuteException;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author svnee
 * 默认下载执行器时间
 * 如果需要进行其他业务逻辑也可自行实现,比如：报警通知等等
 **/
@Slf4j
public class DefaultDownloadRejectExecutionHandler implements BaseDefaultDownloadRejectExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.warn("[DefaultDownloadRejectExecutionHandler] reject execution,r:{}", r.toString());
        throw new DownloadRejectExecuteException(DownloadErrorCode.DOWNLOAD_OVER_WAIT_NUM_REJECT);
    }

}
