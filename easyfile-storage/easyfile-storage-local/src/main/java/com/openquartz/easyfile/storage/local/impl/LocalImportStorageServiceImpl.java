package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.DataExecuteErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.common.util.PageUtil;
import com.openquartz.easyfile.common.util.PaginationUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.local.convertor.AsyncImportRecordConverter;
import com.openquartz.easyfile.storage.local.entity.AsyncImportAppEntity;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTask;
import com.openquartz.easyfile.storage.local.exception.ImportStorageErrorCode;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTaskMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.ImportRecordQueryCondition;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地导入存储
 *
 * @author svnee
 */
@Slf4j
public class LocalImportStorageServiceImpl implements ImportStorageService {

    private final AsyncImportTaskMapper asyncImportTaskMapper;
    private final AsyncImportRecordMapper asyncImportRecordMapper;
    private final IdempotentImportManager idempotentImportManager;

    public LocalImportStorageServiceImpl(
        AsyncImportTaskMapper asyncImportTaskMapper,
        AsyncImportRecordMapper asyncImportRecordMapper) {
        this(asyncImportTaskMapper, asyncImportRecordMapper, null);
    }

    public LocalImportStorageServiceImpl(
        AsyncImportTaskMapper asyncImportTaskMapper,
        AsyncImportRecordMapper asyncImportRecordMapper,
        IdempotentImportManager idempotentImportManager) {
        this.asyncImportTaskMapper = asyncImportTaskMapper;
        this.asyncImportRecordMapper = asyncImportRecordMapper;
        this.idempotentImportManager = idempotentImportManager;
    }

    @Override
    public boolean enableRunning(Long registerId) {
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(registerId);
        if (Objects.isNull(importRecord) || importRecord.getImportStatus() != ImportStatusEnum.NONE) {
            return false;
        }
        int affect = asyncImportRecordMapper
            .refreshImportStatus(registerId, ImportStatusEnum.NONE, ImportStatusEnum.EXECUTING,
                importRecord.getUpdateBy());
        return affect > 0;
    }

    @Override
    public Long register(ImportRegisterRequest request) {
        AsyncImportTask importTask = asyncImportTaskMapper
            .selectByImportCode(request.getImportCode(), request.getAppId());
        if (Objects.isNull(importTask)) {
            throw new EasyFileException(ImportStorageErrorCode.IMPORT_TASK_NOT_EXIST);
        }
        AsyncImportRecord importRecord = buildRegisterDefaultImportRecord(request, importTask);
        int affect = asyncImportRecordMapper.insertSelective(importRecord);
        Asserts.isTrue(affect > 0, DataExecuteErrorCode.INSERT_ERROR);
        return importRecord.getId();
    }

    private AsyncImportRecord buildRegisterDefaultImportRecord(ImportRegisterRequest request,
                                                               AsyncImportTask importTask) {
        AsyncImportRecord importRecord = new AsyncImportRecord();
        importRecord.setImportTaskId(importTask.getId());
        importRecord.setAppId(request.getAppId());
        importRecord.setImportCode(request.getImportCode());
        importRecord.setImportStatus(ImportStatusEnum.NONE);
        importRecord.setSourceFileUrl(StringUtils.isNotBlank(request.getOssUrl()) ? request.getOssUrl() : StringUtils.EMPTY);
        importRecord.setSourceFileName(
            StringUtils.isNotBlank(request.getFileName()) ? request.getFileName() : StringUtils.EMPTY);
        importRecord.setFileType(StringUtils.isNotBlank(request.getFileType()) ? request.getFileType() : StringUtils.EMPTY);
        importRecord.setFileHash(StringUtils.EMPTY);
        importRecord.setBusinessUniqueKey(
            StringUtils.isNotBlank(request.getBusinessUniqueKey()) ? request.getBusinessUniqueKey() : StringUtils.EMPTY);
        importRecord.setResultFileUrl(StringUtils.EMPTY);
        importRecord.setTotalCount(0L);
        importRecord.setSuccessCount(0L);
        importRecord.setFailCount(0L);
        importRecord.setExecuteProcess(0);
        importRecord.setResumePosition(0L);
        importRecord.setNotifyEnableStatus(Boolean.TRUE.equals(request.getEnableNotify())
            ? EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
        importRecord.setNotifyEmail(
            StringUtils.isNotBlank(request.getNotifyEmail()) ? request.getNotifyEmail() : StringUtils.EMPTY);
        importRecord.setMaxServerRetry(request.getMaxRetry() == null ? 0 : request.getMaxRetry());
        importRecord.setCurrentRetry(0);
        importRecord.setExecuteParam(JSONUtil.toClassJson(request));
        importRecord.setErrorMsg(StringUtils.EMPTY);
        importRecord.setLastExecuteTime(new Date());
        importRecord.setVersion(Constants.DATA_INIT_VERSION);
        importRecord.setCreateTime(new Date());
        importRecord.setUpdateTime(new Date());
        String operator = StringUtils.isNotBlank(request.getCreateBy()) ? request.getCreateBy() : Constants.SYSTEM_USER;
        importRecord.setCreateBy(operator);
        importRecord.setUpdateBy(operator);
        importRecord.setLocale(StringUtils.isNotBlank(request.getLocale()) ? request.getLocale() : StringUtils.EMPTY);
        return importRecord;
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        if (Objects.isNull(request) || MapUtils.isEmpty(request.getImportCodeMap())) {
            return;
        }
        Map<String, AsyncImportTask> importTaskMap = buildDefaultAsyncImportTask(request);

        List<AsyncImportTask> importTaskList = asyncImportTaskMapper
            .listByImportCode(CollectionUtils.newArrayList(importTaskMap.keySet()),
                Collections.singletonList(request.getAppId()));
        Map<String, AsyncImportTask> existImportTaskMap = importTaskList.stream()
            .collect(Collectors.toMap(AsyncImportTask::getTaskCode, Function.identity()));

        for (Entry<String, String> entry : request.getImportCodeMap().entrySet()) {
            AsyncImportTask existedAsyncImportTask = existImportTaskMap.get(entry.getKey());
            if (Objects.isNull(existedAsyncImportTask)) {
                asyncImportTaskMapper.insertSelective(importTaskMap.get(entry.getKey()));
            } else if (!existedAsyncImportTask.getTaskDesc().equals(entry.getValue())) {
                asyncImportTaskMapper.refreshTaskDesc(existedAsyncImportTask.getId(), entry.getValue());
            }
        }
    }

    private Map<String, AsyncImportTask> buildDefaultAsyncImportTask(AutoTaskRegisterRequest request) {
        return request.getImportCodeMap().entrySet().stream().map(e -> {
            AsyncImportTask asyncImportTask = new AsyncImportTask();
            asyncImportTask.setTaskCode(e.getKey());
            asyncImportTask.setTaskDesc(e.getValue());
            asyncImportTask.setAppId(request.getAppId());
            asyncImportTask.setUnifiedAppId(request.getUnifiedAppId());
            asyncImportTask.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
            asyncImportTask.setVersion(Constants.DATA_INIT_VERSION);
            asyncImportTask.setCreateTime(new Date());
            asyncImportTask.setUpdateTime(new Date());
            asyncImportTask.setCreateBy(Constants.SYSTEM_USER);
            asyncImportTask.setUpdateBy(Constants.SYSTEM_USER);
            asyncImportTask.setIsDeleted(0L);
            return asyncImportTask;
        }).collect(Collectors.toMap(AsyncImportTask::getTaskCode, Function.identity()));
    }

    @Override
    public ImportRequestInfo getRequestInfoByRegisterId(Long registerId) {
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(registerId);
        if (Objects.isNull(importRecord)) {
            return null;
        }
        ImportRegisterRequest registerRequest = JSONUtil
            .parseClassObject(importRecord.getExecuteParam(), ImportRegisterRequest.class);
        if (Objects.isNull(registerRequest)) {
            registerRequest = ImportRegisterRequest.builder()
                .appId(importRecord.getAppId())
                .importCode(importRecord.getImportCode())
                .ossUrl(importRecord.getSourceFileUrl())
                .fileName(importRecord.getSourceFileName())
                .fileType(importRecord.getFileType())
                .businessUniqueKey(importRecord.getBusinessUniqueKey())
                .executeParam(importRecord.getExecuteParam())
                .createBy(importRecord.getCreateBy())
                .maxRetry(importRecord.getMaxServerRetry())
                .notifyEmail(importRecord.getNotifyEmail())
                .locale(importRecord.getLocale())
                .enableNotify(Objects.equals(importRecord.getNotifyEnableStatus(), EnableStatusEnum.ENABLE.getCode()))
                .build();
        } else if (StringUtils.isBlank(registerRequest.getOssUrl())) {
            registerRequest.setOssUrl(importRecord.getSourceFileUrl());
        }

        ImportRequestInfo requestInfo = new ImportRequestInfo();
        requestInfo.setImportCode(importRecord.getImportCode());
        requestInfo.setRegisterRequest(registerRequest);
        requestInfo.setResumePosition(importRecord.getResumePosition());
        requestInfo.setImportStatus(importRecord.getImportStatus());
        requestInfo.setExecuteProcess(importRecord.getExecuteProcess());
        requestInfo.setTotalCount(importRecord.getTotalCount());
        requestInfo.setSuccessCount(importRecord.getSuccessCount());
        requestInfo.setFailCount(importRecord.getFailCount());
        requestInfo.setResultFileUrl(importRecord.getResultFileUrl());
        requestInfo.setErrorMsg(importRecord.getErrorMsg());
        return requestInfo;
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                       ImportStatusEnum nextStatus) {
        if (executeProcess == null || executeProcess <= 0) {
            return;
        }
        asyncImportRecordMapper.refreshExecuteProcess(registerId, executeProcess,
            resumePosition == null ? 0L : resumePosition, nextStatus);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        asyncImportRecordMapper.resetExecuteProcess(registerId);
    }

    @Override
    public void updateTotalCount(Long registerId, Long totalCount) {
        asyncImportRecordMapper.updateTotalCount(registerId, totalCount);
    }

    @Override
    public void updateFileHash(Long registerId, String fileHash) {
        asyncImportRecordMapper.updateFileHash(registerId, fileHash);
    }

    @Override
    public void updateResultFileUrl(Long registerId, String resultFileUrl) {
        asyncImportRecordMapper.updateResultFileUrl(registerId, resultFileUrl);
    }

    @Override
    public void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                         Long failCount, Long resumePosition, String errorMsg) {
        asyncImportRecordMapper.complete(registerId, status, totalCount, successCount, failCount, resumePosition,
            errorMsg);
    }

    @Override
    public CancelImportResult cancelImport(CancelImportRequest request) {
        CancelImportResult cancelResult = new CancelImportResult();
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(request.getRegisterId());
        if (Objects.isNull(importRecord)) {
            cancelResult.setCancelResult(false);
            cancelResult.setCancelMsg("任务不存在");
            return cancelResult;
        }
        if (importRecord.getImportStatus() != ImportStatusEnum.NONE) {
            cancelResult.setCancelResult(false);
            cancelResult.setCancelMsg("已经执行开始执行无法撤销");
            return cancelResult;
        }
        int affect = asyncImportRecordMapper.refreshImportStatus(request.getRegisterId(),
            ImportStatusEnum.NONE, ImportStatusEnum.CANCEL, request.getCancelBy());
        cancelResult.setCancelResult(affect > 0);
        cancelResult.setCancelMsg(affect > 0 ? "撤销成功" : "撤销失败");
        return cancelResult;
    }

    @Override
    public Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 20 : request.getPageSize();

        // Require app scope; blank/null appId must not list all apps' rows (match download safe scoping).
        if (StringUtils.isBlank(request.getAppId())) {
            return PaginationUtils.empty(pageNum, pageSize);
        }

        ImportRecordQueryCondition condition = new ImportRecordQueryCondition();
        condition.setLimitedAppIdList(Collections.singletonList(request.getAppId()));
        condition.setImportCode(request.getImportCode());
        condition.setCreateBy(request.getCreateBy());
        condition.setBusinessUniqueKey(request.getBusinessUniqueKey());
        condition.setStartCreateTime(request.getStartTime());
        condition.setEndCreateTime(request.getEndTime());
        if (StringUtils.isNotBlank(request.getImportStatus())) {
            condition.setImportStatus(ImportStatusEnum.fromCode(request.getImportStatus()));
        }

        int total = asyncImportRecordMapper.countByCondition(condition);
        if (total <= 0) {
            return PaginationUtils.empty(pageNum, pageSize);
        }

        condition.setStartOffset(Pair.of(PageUtil.startIndex(pageNum, pageSize), pageSize));
        List<AsyncImportRecord> recordList = asyncImportRecordMapper.selectByCondition(condition);
        List<ImportResult> resultList = recordList.stream()
            .map(AsyncImportRecordConverter::convert)
            .collect(Collectors.toList());

        Pagination<ImportResult> pagination = new Pagination<>();
        pagination.setModelList(resultList);
        pagination.setTotalRecords((long) total);
        pagination.setPage(Long.valueOf(pageNum));
        pagination.setPageSize(Long.valueOf(pageSize));
        return pagination;
    }

    @Override
    public boolean retry(Long registerId, String operator) {
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(registerId);
        if (Objects.isNull(importRecord) || !isRetryableStatus(importRecord.getImportStatus())) {
            return false;
        }
        int affect = asyncImportRecordMapper.refreshImportStatus(registerId,
            importRecord.getImportStatus(), ImportStatusEnum.NONE, operator);
        if (affect > 0) {
            asyncImportRecordMapper.resetExecuteProcess(registerId);
            clearIdempotentMark(importRecord.getBusinessUniqueKey());
        }
        return affect > 0;
    }

    private void clearIdempotentMark(String businessUniqueKey) {
        if (idempotentImportManager == null || StringUtils.isBlank(businessUniqueKey)) {
            return;
        }
        idempotentImportManager.clear(businessUniqueKey);
    }

    /**
     * Terminal-but-recoverable statuses: FAIL / CANCEL (code revoke) / PARTIAL_SUCCESS.
     */
    private static boolean isRetryableStatus(ImportStatusEnum status) {
        return status == ImportStatusEnum.FAIL
            || status == ImportStatusEnum.CANCEL
            || status == ImportStatusEnum.PARTIAL_SUCCESS;
    }

    @Override
    public boolean revoke(Long registerId, String operator) {
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(registerId);
        if (Objects.isNull(importRecord)) {
            return false;
        }
        if (importRecord.getImportStatus() == ImportStatusEnum.NONE) {
            return asyncImportRecordMapper.refreshImportStatus(registerId,
                ImportStatusEnum.NONE, ImportStatusEnum.CANCEL, operator) > 0;
        }
        return false;
    }

    @Override
    public DownloadUrlResult resultFile(Long registerId) {
        AsyncImportRecord importRecord = asyncImportRecordMapper.findById(registerId);
        if (Objects.isNull(importRecord) || StringUtils.isBlank(importRecord.getResultFileUrl())) {
            return null;
        }
        DownloadUrlResult downloadUrlResult = new DownloadUrlResult();
        downloadUrlResult.setUrl(importRecord.getResultFileUrl());
        downloadUrlResult.setFileName("import_result_" + registerId + ".xlsx");
        downloadUrlResult.setFileSystem("local");
        return downloadUrlResult;
    }

    @Override
    public List<AppTree> getAppTree() {
        List<AsyncImportAppEntity> entityList = asyncImportTaskMapper.getAsyncImportAppEntity();
        return entityList.stream()
            .collect(Collectors.groupingBy(AsyncImportAppEntity::getUnifiedAppId,
                Collectors.mapping(AsyncImportAppEntity::getAppId, Collectors.toList())))
            .entrySet()
            .stream()
            .map(e -> new AppTree(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
