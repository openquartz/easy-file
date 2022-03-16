package org.svnee.easyfile.starter.executor.impl;

import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.starter.exception.DownloadErrorCode;
import org.svnee.easyfile.starter.exception.DownloadRejectExecuteException;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;

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
