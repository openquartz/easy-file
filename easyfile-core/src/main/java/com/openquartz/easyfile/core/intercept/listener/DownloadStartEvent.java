package com.openquartz.easyfile.core.intercept.listener;

import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;

/**
 * 下载开始事件
 *
 * @author svnee
 */
public class DownloadStartEvent {

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
     * 本次traceId
     */
    private final String downloadTraceId;

    public DownloadStartEvent(DownloaderRequestContext requestContext,
        BaseDownloadExecutor executor,
        boolean enableAsync,
        String downloadTraceId) {

        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
        this.downloadTraceId = downloadTraceId;
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

    public String getDownloadTraceId() {
        return downloadTraceId;
    }

    @Override
    public String toString() {
        return "DownloadStartEvent{" +
            "requestContext=" + requestContext +
            ", executor=" + executor +
            ", enableAsync=" + enableAsync +
            ", downloadTraceId='" + downloadTraceId + '\'' +
            '}';
    }
}
