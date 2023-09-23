package com.openquartz.easyfile.core.intercept.listener;

import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;

/**
 * 下载结束事件
 *
 * @author svnee
 */
public class DownloadEndEvent {

    /**
     * 请求上下文
     */
    private final DownloaderRequestContext requestContext;

    /**
     * 下载执行器
     */
    private final BaseDownloadExecutor executor;

    /**
     * 开启异步
     */
    private final boolean enableAsync;

    /**
     * 异常
     */
    private final Throwable exception;

    /**
     * 执行结果
     */
    private final Object executeResult;

    /**
     * download traceId
     */
    private final String downloadTraceId;

    public DownloadEndEvent(DownloaderRequestContext requestContext,
        BaseDownloadExecutor executor,
        boolean enableAsync,
        Throwable exception,
        Object executeResult,
        String downloadTraceId) {
        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
        this.executeResult = executeResult;
        this.exception = exception;
        this.downloadTraceId = downloadTraceId;
    }

    public Throwable getException() {
        return exception;
    }

    public DownloaderRequestContext getRequestContext() {
        return requestContext;
    }

    public BaseDownloadExecutor getExecutor() {
        return executor;
    }

    public boolean isEnableAsync() {
        return enableAsync;
    }

    public Object getExecuteResult() {
        return executeResult;
    }

    public String getDownloadTraceId() {
        return downloadTraceId;
    }

    @Override
    public String toString() {
        return "DownloadEndEvent{" +
            "requestContext=" + requestContext +
            ", executor=" + executor +
            ", enableAsync=" + enableAsync +
            ", exception=" + exception +
            ", executeResult=" + executeResult +
            ", downloadTraceId='" + downloadTraceId + '\'' +
            '}';
    }
}
