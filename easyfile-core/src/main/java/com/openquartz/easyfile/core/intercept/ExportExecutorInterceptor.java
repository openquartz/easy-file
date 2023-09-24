package com.openquartz.easyfile.core.intercept;

import com.openquartz.easyfile.common.bean.BaseExporterRequestContext;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;

/**
 * 下载处理器拦截器
 *
 * @author svnee
 */
public interface ExportExecutorInterceptor {

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
    void beforeExecute(BaseExportExecutor executor, BaseExporterRequestContext context, Long registerId,
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
    void afterExecute(BaseExportExecutor executor, BaseExporterRequestContext context, ExportResult result,
        InterceptorContext interceptorContext);
}
