package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;

/**
 * 异步导入处理器
 * 可直接继承 {@link AsyncImportHandlerAdapter}
 *
 * @author svnee
 */
public interface BaseAsyncImportHandler {

    /**
     * 异步导入处理（是否成功）
     *
     * @param executor 导入执行器
     * @param context 请求上下文
     * @param registerId 注册ID
     * @return 是否成功
     */
    default boolean handle(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        ImportProcessResult result = handleResult(executor, context, registerId);
        return result != null && result.isSuccess();
    }

    /**
     * 异步导入处理（完整结果）
     *
     * @param executor 导入执行器
     * @param context 请求上下文
     * @param registerId 注册ID
     * @return 处理结果
     */
    ImportProcessResult handleResult(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId);

    /**
     * 外部触发执行（默认同步走 handle）
     *
     * @param executor 导入执行器
     * @param context 请求上下文
     * @param registerId 注册ID
     */
    default void execute(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        handle(executor, context, registerId);
    }
}
