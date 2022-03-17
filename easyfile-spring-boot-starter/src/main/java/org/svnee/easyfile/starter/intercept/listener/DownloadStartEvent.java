package org.svnee.easyfile.starter.intercept.listener;

import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;

/**
 * 下载开始事件
 *
 * @author xuzhao
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

    public DownloadStartEvent(DownloaderRequestContext requestContext,
        BaseDownloadExecutor executor, boolean enableAsync) {
        this.requestContext = requestContext;
        this.executor = executor;
        this.enableAsync = enableAsync;
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

    @Override
    public String toString() {
        return "DownloadStartEvent{" +
            "requestContext=" + requestContext +
            ", executor=" + executor +
            ", enableAsync=" + enableAsync +
            '}';
    }
}
