package com.openquartz.easyfile.core.intercept.listener;

import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;

/**
 * 导入开始事件
 *
 * @author svnee
 */
public class ImportStartEvent {

    /**
     * 请求上下文
     */
    private final ImportRequestContext requestContext;

    /**
     * 导入执行器
     */
    private final BaseImportExecutor<?> executor;

    /**
     * 开启异步
     */
    private final boolean enableAsync;

    /**
     * 本次traceId
     */
    private final String importTraceId;

    public ImportStartEvent(ImportRequestContext requestContext,
        BaseImportExecutor<?> executor,
        boolean enableAsync,
        String importTraceId) {

        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
        this.importTraceId = importTraceId;
    }

    public ImportRequestContext getRequestContext() {
        return requestContext;
    }

    public BaseImportExecutor<?> getExecutor() {
        return executor;
    }

    public boolean isEnableAsync() {
        return enableAsync;
    }

    public String getImportTraceId() {
        return importTraceId;
    }

    @Override
    public String toString() {
        return "ImportStartEvent{" +
            "requestContext=" + requestContext +
            ", executor=" + executor +
            ", enableAsync=" + enableAsync +
            ", importTraceId='" + importTraceId + '\'' +
            '}';
    }
}
