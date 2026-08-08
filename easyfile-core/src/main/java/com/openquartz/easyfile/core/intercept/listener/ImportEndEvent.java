package com.openquartz.easyfile.core.intercept.listener;

import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;

/**
 * 导入结束事件
 *
 * @author svnee
 */
public class ImportEndEvent {

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
     * 异常
     */
    private final Throwable exception;

    /**
     * 执行结果
     */
    private final Object executeResult;

    /**
     * import traceId
     */
    private final String importTraceId;

    public ImportEndEvent(ImportRequestContext requestContext,
        BaseImportExecutor<?> executor,
        boolean enableAsync,
        Throwable exception,
        Object executeResult,
        String importTraceId) {
        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
        this.executeResult = executeResult;
        this.exception = exception;
        this.importTraceId = importTraceId;
    }

    public Throwable getException() {
        return exception;
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

    public Object getExecuteResult() {
        return executeResult;
    }

    public String getImportTraceId() {
        return importTraceId;
    }

    @Override
    public String toString() {
        return "ImportEndEvent{" +
            "requestContext=" + requestContext +
            ", executor=" + executor +
            ", enableAsync=" + enableAsync +
            ", exception=" + exception +
            ", executeResult=" + executeResult +
            ", importTraceId='" + importTraceId + '\'' +
            '}';
    }
}
