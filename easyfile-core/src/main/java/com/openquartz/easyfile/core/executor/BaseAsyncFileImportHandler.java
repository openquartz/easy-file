package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseImportRequestContext;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.response.ImportResult;

/**
 * 异步文件处理器
 * 用于文件异步处理
 * 你可以直接继承 {@link com.openquartz.easyfile.core.executor.AsyncFileImportHandlerAdapter},
 *
 * @author svnee
 */
public interface BaseAsyncFileImportHandler extends FileHandlerExecutor<BaseImportExecutor, BaseImportRequestContext> {

    /**
     * 异步文件处理器
     *
     * @param executor    下载执行器
     * @param baseRequest 基础请求
     * @param registerId  注册下载任务ID
     * @return 处理结果 成功/失败
     */
    default boolean handle(BaseImportExecutor executor, BaseImportRequestContext baseRequest, Long registerId) {
        ImportResult importResult = handleResult(executor, baseRequest, registerId);
        return HandleStatusEnum.SUCCESS.equals(importResult.getHandleStatus());
    }

    /**
     * 异步文件处理器
     *
     * @param executor    下载执行器
     * @param baseRequest 基础请求
     * @param registerId  注册下载任务ID
     * @return 处理结果 成功/失败
     */
    ImportResult handleResult(BaseImportExecutor executor, BaseImportRequestContext baseRequest, Long registerId);

    /**
     * 异步文件执行器
     * 外部调用触发执行器
     *
     * @param executor    下载执行器
     * @param baseRequest 基础请求
     * @param registerId  注册下载任务ID
     */
    default void execute(BaseImportExecutor executor, BaseImportRequestContext baseRequest, Long registerId) {
        handle(executor, baseRequest, registerId);
    }
}
