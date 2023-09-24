package com.openquartz.easyfile.core.intercept.listener;

import com.openquartz.easyfile.common.bean.ExporterRequestContext;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;

/**
 * 下载开始事件
 *
 * @author svnee
 */
public class ExportStartEvent {

    /**
     * 请求上下文
     */
    private final ExporterRequestContext requestContext;

    /**
     * 下载执行器
     */
    private final BaseExportExecutor executor;

    /**
     * 开启异步
     */
    private final boolean enableAsync;

    /**
     * 本次traceId
     */
    private final String downloadTraceId;

    public ExportStartEvent(ExporterRequestContext requestContext,
        BaseExportExecutor executor,
        boolean enableAsync,
        String downloadTraceId) {

        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
        this.downloadTraceId = downloadTraceId;
    }

    public ExporterRequestContext getRequestContext() {
        return requestContext;
    }

    public BaseExportExecutor getExecutor() {
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
