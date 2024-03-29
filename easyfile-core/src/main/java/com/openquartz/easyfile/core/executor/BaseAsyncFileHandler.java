package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.response.ExportResult;

/**
 * 异步文件处理器
 * 用于文件异步处理
 * 你可以直接继承 {@link AsyncFileHandlerAdapter},
 *
 * @author svnee
 */
public interface BaseAsyncFileHandler {

    /**
     * 异步文件处理器
     *
     * @param executor 下载执行器
     * @param baseRequest 基础请求
     * @param registerId 注册下载任务ID
     * @return 处理结果 成功/失败
     */
    default boolean handle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        ExportResult exportResult = handleResult(executor, baseRequest, registerId);
        return UploadStatusEnum.SUCCESS.equals(exportResult.getUploadStatus());
    }

    /**
     * 异步文件处理器
     *
     * @param executor 下载执行器
     * @param baseRequest 基础请求
     * @param registerId 注册下载任务ID
     * @return 处理结果 成功/失败
     */
    ExportResult handleResult(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId);

    /**
     * 异步文件执行器
     * 外部调用触发执行器
     *
     * @param executor 下载执行器
     * @param baseRequest 基础请求
     * @param registerId 注册下载任务ID
     */
    default void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        handle(executor, baseRequest, registerId);
    }
}
