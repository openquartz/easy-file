package org.svnee.easyfile.starter.intercept;

import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;

/**
 * 下载处理器拦截器
 *
 * @author svnee
 */
public interface DownloadExecutorInterceptor {

    /**
     * 拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 执行之前处理
     *
     * @param executor 执行器
     * @param context 上下文
     * @param registerId 注册下载任务ID
     * @param interceptorContext 拦截上下文
     */
    void beforeExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, Long registerId,
        InterceptorContext interceptorContext);

    /**
     * 执行之后处理
     *
     * @param executor 执行器
     * @param context 上下文
     * @param result 导出结果
     * @param interceptorContext 拦截上下文
     */
    void afterExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, ExportResult result,
        InterceptorContext interceptorContext);
}
