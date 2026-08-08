package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.server.notify.NotifyMessageTemplate;
import com.openquartz.easyfile.server.service.ImportService;
import com.openquartz.easyfile.server.service.NotifyService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.imports.ImportTriggerService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 导入服务实现
 *
 * @author svnee
 */
@Slf4j
@Service
public class ImportServiceImpl implements ImportService {

    private final ImportStorageService importStorageService;
    private final ImportTriggerService importTriggerService;
    private final NotifyService notifyService;

    public ImportServiceImpl(ImportStorageService importStorageService,
                             @Autowired(required = false) ImportTriggerService importTriggerService,
                             @Autowired(required = false) NotifyService notifyService) {
        this.importStorageService = importStorageService;
        this.importTriggerService = importTriggerService;
        this.notifyService = notifyService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerImport(ImportRegisterRequest request) {
        validateImportRequest(request);
        Long registerId = importStorageService.register(request);
        if (Boolean.TRUE.equals(request.getEnableAsync()) && importTriggerService != null) {
            ImportTriggerRequest triggerRequest = new ImportTriggerRequest();
            triggerRequest.setRegisterId(registerId);
            if (request.getMaxRetry() != null) {
                triggerRequest.setMaxTriggerCount(request.getMaxRetry());
            }
            importTriggerService.trigger(triggerRequest);
        }
        log.info("导入任务注册成功: registerId={}, appId={}, importCode={}",
            registerId, request.getAppId(), request.getImportCode());
        return registerId;
    }

    @Override
    public boolean executeImport(Long registerId) {
        try {
            return importStorageService.enableRunning(registerId);
        } catch (Exception e) {
            log.error("导入任务执行失败: registerId={}", registerId, e);
            return false;
        }
    }

    @Override
    public ImportResult queryImportStatus(Long registerId) {
        return toImportResult(registerId, importStorageService.getRequestInfoByRegisterId(registerId));
    }

    @Override
    public ImportResult queryImportProgress(Long registerId) {
        return toImportResult(registerId, importStorageService.getRequestInfoByRegisterId(registerId));
    }

    @Override
    public Pagination<ImportResult> listImportRecords(ImportResultQueryRequest request) {
        return importStorageService.listImportResult(request);
    }

    @Override
    public boolean retryImport(Long registerId, String operator) {
        boolean ok = importStorageService.retry(registerId, operator);
        if (ok && importTriggerService != null) {
            ImportTriggerRequest triggerRequest = new ImportTriggerRequest();
            triggerRequest.setRegisterId(registerId);
            importTriggerService.trigger(triggerRequest);
        }
        return ok;
    }

    @Override
    public boolean revokeImport(Long registerId, String operator) {
        return importStorageService.revoke(registerId, operator);
    }

    @Override
    public DownloadUrlResult getResultFile(Long registerId) {
        DownloadUrlResult result = importStorageService.resultFile(registerId);
        if (result == null) {
            throw new RuntimeException("结果文件不存在");
        }
        return result;
    }

    @Override
    public List<AppTree> getAppTree() {
        List<AppTree> appTree = importStorageService.getAppTree();
        return appTree == null ? Collections.emptyList() : appTree;
    }

    @Override
    public boolean enableRunning(Long registerId) {
        return importStorageService.enableRunning(registerId);
    }

    @Override
    public ImportRequestInfo getRequestInfoByRegisterId(Long registerId) {
        return importStorageService.getRequestInfoByRegisterId(registerId);
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                       ImportStatusEnum nextStatus) {
        importStorageService.refreshExecuteProgress(registerId, executeProcess, resumePosition, nextStatus);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        importStorageService.resetExecuteProcess(registerId);
    }

    @Override
    public void updateTotalCount(Long registerId, Long totalCount) {
        importStorageService.updateTotalCount(registerId, totalCount);
    }

    @Override
    public void updateFileHash(Long registerId, String fileHash) {
        importStorageService.updateFileHash(registerId, fileHash);
    }

    @Override
    public void updateResultFileUrl(Long registerId, String resultFileUrl) {
        importStorageService.updateResultFileUrl(registerId, resultFileUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                         Long failCount, Long resumePosition, String errorMsg) {
        ImportRequestInfo requestInfo = importStorageService.getRequestInfoByRegisterId(registerId);
        importStorageService.complete(registerId, status, totalCount, successCount, failCount, resumePosition,
            errorMsg);
        maybeNotify(registerId, status, requestInfo, errorMsg);
    }

    @Override
    public CancelImportResult cancelImport(CancelImportRequest request) {
        return importStorageService.cancelImport(request);
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        importStorageService.autoRegisterTask(request);
    }

    private void maybeNotify(Long registerId, ImportStatusEnum status, ImportRequestInfo requestInfo, String errorMsg) {
        if (notifyService == null || requestInfo == null || requestInfo.getRegisterRequest() == null) {
            return;
        }
        if (status != ImportStatusEnum.SUCCESS
            && status != ImportStatusEnum.PARTIAL_SUCCESS
            && status != ImportStatusEnum.FAIL) {
            return;
        }
        ImportRegisterRequest registerRequest = requestInfo.getRegisterRequest();
        if (!Boolean.TRUE.equals(registerRequest.getEnableNotify())) {
            return;
        }

        Notifier notifier = new Notifier();
        notifier.setUserBy(registerRequest.getCreateBy());
        notifier.setUserName(registerRequest.getCreateBy());
        notifier.setEmail(registerRequest.getNotifyEmail());

        NotifyMessageTemplate template = new NotifyMessageTemplate();
        template.setTitle(requestInfo.getImportCode());
        template.setContent(StringUtils.isNotBlank(requestInfo.getResultFileUrl())
            ? requestInfo.getResultFileUrl() : StringUtils.EMPTY);
        template.setRegisterId(registerId);
        template.setDownloadTime(new Date());
        template.setDownloadFinishedTime(new Date());
        template.setErrorMsg(errorMsg);
        template.setUploadStatus(status == ImportStatusEnum.FAIL ? UploadStatusEnum.FAIL : UploadStatusEnum.SUCCESS);
        notifyService.notify(notifier, template);
    }

    private ImportResult toImportResult(Long registerId, ImportRequestInfo info) {
        if (info == null) {
            throw new RuntimeException("导入任务不存在: " + registerId);
        }
        ImportStatusEnum status = info.getImportStatus();
        boolean success = status == ImportStatusEnum.SUCCESS || status == ImportStatusEnum.PARTIAL_SUCCESS;
        return ImportResult.builder()
            .registerId(registerId)
            .success(success)
            .status(status == null ? StringUtils.EMPTY : status.getCode())
            .process(info.getExecuteProcess() == null ? 0 : info.getExecuteProcess())
            .totalCount(info.getTotalCount())
            .successCount(info.getSuccessCount())
            .failCount(info.getFailCount())
            .resultFileUrl(info.getResultFileUrl())
            .errorMsg(info.getErrorMsg())
            .build();
    }

    private void validateImportRequest(ImportRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("导入请求不能为空");
        }
        if (StringUtils.isBlank(request.getAppId())) {
            throw new IllegalArgumentException("应用ID不能为空");
        }
        if (StringUtils.isBlank(request.getImportCode())) {
            throw new IllegalArgumentException("导入编码不能为空");
        }
        if (StringUtils.isBlank(request.getOssUrl())) {
            throw new IllegalArgumentException("OSS文件URL不能为空");
        }
        if (StringUtils.isBlank(request.getFileName())) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (StringUtils.isBlank(request.getFileType())) {
            throw new IllegalArgumentException("文件类型不能为空");
        }
        String fileType = request.getFileType().toLowerCase();
        if (!"xlsx".equals(fileType) && !"xls".equals(fileType) && !"csv".equals(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + request.getFileType());
        }
        if (request.getPageSize() != null && request.getPageSize() <= 0) {
            throw new IllegalArgumentException("分页大小必须大于0");
        }
        if (request.getMaxRetry() != null && request.getMaxRetry() < 0) {
            throw new IllegalArgumentException("最大重试次数不能为负数");
        }
    }
}
