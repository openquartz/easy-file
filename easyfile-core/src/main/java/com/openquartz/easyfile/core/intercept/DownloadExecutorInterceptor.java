package com.openquartz.easyfile.core.intercept;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;

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
     * 执行顺序: 按照{@link #order()} 从小到大 顺序执行
     *
     * @param executor 执行器
     * @param context 上下文
     * @param registerId 注册下载任务ID
     * @param interceptorContext 拦截上下文. 用于拦截时之前前后的上下文传递。
     */
    void beforeExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, Long registerId,
        InterceptorContext interceptorContext);

    /**
     * 执行之后处理
     * 执行顺序: 按照{@link #order()} 从大到小 倒序执行
     *
     * @param executor 执行器
     * @param context 上下文
     * @param result 导出结果
     * @param interceptorContext 拦截上下文.用于拦截时之前前后的上下文传递。
     */
    void afterExecute(BaseDownloadExecutor executor, BaseDownloaderRequestContext context, ExportResult result,
        InterceptorContext interceptorContext);
}
