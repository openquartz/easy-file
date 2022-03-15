package org.svnee.easyfile.core.executor.impl;

import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.core.exception.DownloadRejectExecuteException;
import org.svnee.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;

/**
 * @author xuzhao
 * 默认下载执行器时间
 * 如果需要进行其他业务逻辑也可自行实现,比如：报警通知等等
 **/
@Slf4j
public class DefaultDownloadRejectExecutionHandler implements BaseDefaultDownloadRejectExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.warn("[DefaultDownloadRejectExecutionHandler] reject execution,r:{}", r.toString());
        throw new DownloadRejectExecuteException("Reject download exception!");
    }

}
