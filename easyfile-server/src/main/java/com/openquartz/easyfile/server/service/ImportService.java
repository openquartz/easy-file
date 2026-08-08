package com.openquartz.easyfile.server.service;

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
import java.util.List;

/**
 * 导入服务接口
 *
 * @author svnee
 */
public interface ImportService {

    Long registerImport(ImportRegisterRequest request);

    boolean executeImport(Long registerId);

    ImportResult queryImportStatus(Long registerId);

    ImportResult queryImportProgress(Long registerId);

    Pagination<ImportResult> listImportRecords(ImportResultQueryRequest request);

    boolean retryImport(Long registerId, String operator);

    boolean revokeImport(Long registerId, String operator);

    DownloadUrlResult getResultFile(Long registerId);

    List<AppTree> getAppTree();

    boolean enableRunning(Long registerId);

    ImportRequestInfo getRequestInfoByRegisterId(Long registerId);

    void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                ImportStatusEnum nextStatus);

    void resetExecuteProcess(Long registerId);

    void updateTotalCount(Long registerId, Long totalCount);

    void updateFileHash(Long registerId, String fileHash);

    void updateResultFileUrl(Long registerId, String resultFileUrl);

    void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                  Long failCount, Long resumePosition, String errorMsg);

    CancelImportResult cancelImport(CancelImportRequest request);

    void autoRegisterTask(AutoTaskRegisterRequest request);
}
