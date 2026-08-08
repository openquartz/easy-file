package com.openquartz.easyfile.storage.imports;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import java.util.Collections;
import java.util.List;

/**
 * 导入任务状态存储服务
 *
 * @author svnee
 */
public interface ImportStorageService {

    /**
     * 开启执行（CAS 置为执行中）
     *
     * @param registerId 注册ID
     * @return 是否成功
     */
    boolean enableRunning(Long registerId);

    /**
     * 注册导入任务
     *
     * @param request 注册请求
     * @return 注册ID
     */
    Long register(ImportRegisterRequest request);

    /**
     * 自动注册导入任务
     *
     * @param request 请求
     */
    default void autoRegisterTask(AutoTaskRegisterRequest request) {
    }

    /**
     * 根据注册ID查询导入请求信息
     *
     * @param registerId 注册ID
     * @return 请求信息
     */
    ImportRequestInfo getRequestInfoByRegisterId(Long registerId);

    /**
     * 刷新执行进度
     *
     * @param registerId 注册ID
     * @param executeProcess 执行进度
     * @param resumePosition 断点位置
     * @param nextStatus 下一状态
     */
    void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                ImportStatusEnum nextStatus);

    /**
     * 重置执行进度
     *
     * @param registerId 注册ID
     */
    void resetExecuteProcess(Long registerId);

    /**
     * 更新总行数
     *
     * @param registerId 注册ID
     * @param totalCount 总行数
     */
    void updateTotalCount(Long registerId, Long totalCount);

    /**
     * 更新文件哈希
     *
     * @param registerId 注册ID
     * @param fileHash 文件哈希
     */
    void updateFileHash(Long registerId, String fileHash);

    /**
     * 更新结果文件地址
     *
     * @param registerId 注册ID
     * @param resultFileUrl 结果文件地址
     */
    void updateResultFileUrl(Long registerId, String resultFileUrl);

    /**
     * 完成导入任务
     *
     * @param registerId 注册ID
     * @param status 最终状态
     * @param totalCount 总行数
     * @param successCount 成功行数
     * @param failCount 失败行数
     * @param resumePosition 断点位置
     * @param errorMsg 错误信息
     */
    void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                  Long failCount, Long resumePosition, String errorMsg);

    /**
     * 取消导入任务
     *
     * @param request 取消请求
     * @return 取消结果
     */
    CancelImportResult cancelImport(CancelImportRequest request);

    /**
     * 管理端分页查询导入记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    default Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        return Pagination.empty();
    }

    /**
     * 重试导入任务
     *
     * @param registerId 导入记录ID
     * @param operator 操作人
     * @return 是否成功
     */
    default boolean retry(Long registerId, String operator) {
        return false;
    }

    /**
     * 撤销导入任务
     *
     * @param registerId 导入记录ID
     * @param operator 操作人
     * @return 是否成功
     */
    default boolean revoke(Long registerId, String operator) {
        return false;
    }

    /**
     * 获取导入结果文件
     *
     * @param registerId 导入记录ID
     * @return 结果文件下载信息
     */
    default DownloadUrlResult resultFile(Long registerId) {
        return null;
    }

    /**
     * 查询可见应用树
     *
     * @return appTree
     */
    default List<AppTree> getAppTree() {
        return Collections.emptyList();
    }

    /** @deprecated use enableRunning */
    @Deprecated
    default void markExecuting(Long registerId) {
        enableRunning(registerId);
    }

    /** @deprecated use refreshExecuteProgress */
    @Deprecated
    default void refreshProgress(Long registerId, Integer process, Long resumePosition) {
        refreshExecuteProgress(registerId, process, resumePosition, ImportStatusEnum.EXECUTING);
    }
}
