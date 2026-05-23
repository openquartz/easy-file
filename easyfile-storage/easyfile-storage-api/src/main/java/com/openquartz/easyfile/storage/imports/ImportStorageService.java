package com.openquartz.easyfile.storage.imports;

import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
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
     * 自动注册导入任务
     *
     * @param request 请求
     */
    default void autoRegisterTask(AutoTaskRegisterRequest request) {
    }

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

    void markExecuting(Long registerId);

    void refreshProgress(Long registerId, Integer process, Long resumePosition);

    void updateTotalCount(Long registerId, Long totalCount);

    void updateFileHash(Long registerId, String fileHash);

    void updateResultFileUrl(Long registerId, String resultFileUrl);

    void complete(Long registerId, boolean success, Long totalCount, Long successCount,
                  Long failCount, Long resumePosition, String errorMsg);
}
