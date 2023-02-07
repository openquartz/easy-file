package org.svnee.easyfile.storage.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.BaseExecuteParam;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.EnableStatusEnum;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.exception.DataExecuteErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.file.FileUrlTransformer;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.PageUtil;
import org.svnee.easyfile.common.util.PaginationUtils;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.storage.convertor.AsyncDownloadRecordConverter;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.entity.AsyncDownloadRecord;
import org.svnee.easyfile.storage.entity.AsyncDownloadTask;
import org.svnee.easyfile.storage.exception.AsyncDownloadExceptionCode;
import org.svnee.easyfile.storage.exception.DownloadStorageErrorCode;
import org.svnee.easyfile.storage.expand.FileUrlTransformerSupport;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.mapper.condition.BaseRecordQueryCondition;
import org.svnee.easyfile.storage.mapper.condition.UploadInfoChangeCondition;

/**
 * 本地下载存储
 *
 * @author svnee
 **/
@Slf4j
@AllArgsConstructor
public class LocalDownloadStorageServiceImpl implements DownloadStorageService {

    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    private final AsyncDownloadRecordMapper asyncDownloadRecordMapper;

    @Override
    public boolean enableRunning(Long registerId) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(registerId);
        if (Objects.isNull(downloadRecord) || downloadRecord.getUploadStatus() != UploadStatusEnum.NONE) {
            return false;
        }
        boolean running = downloadRecord.getUploadStatus() == UploadStatusEnum.NONE;
        if (Boolean.TRUE.equals(running)) {
            int affect = asyncDownloadRecordMapper
                .refreshUploadStatus(registerId, UploadStatusEnum.NONE, UploadStatusEnum.EXECUTING,
                    downloadRecord.getUpdateBy());
            return affect > 0;
        }
        return false;
    }

    @Override
    public ExportResult loadingCacheExportResult(LoadingExportCacheRequest request) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, DownloadStorageErrorCode.DOWNLOAD_TASK_NOT_EXIST);

        List<AsyncDownloadRecord> downloadRecordList = asyncDownloadRecordMapper
            .listByTaskIdAndStatus(downloadRecord.getDownloadTaskId(), UploadStatusEnum.SUCCESS, 500);

        ExportResult result = Optional.ofNullable(downloadRecordList)
            .orElse(Collections.emptyList())
            .stream()
            .filter(e -> matchCacheKey(request.getCacheKeyList(), request.getExportParamMap(), e))
            .findFirst().map(e -> convertRecord(e, request.getRegisterId()))
            .orElseGet(() -> {
                ExportResult exportResult = new ExportResult();
                exportResult.setRegisterId(request.getRegisterId());
                exportResult.setUploadStatus(UploadStatusEnum.FAIL);
                return exportResult;
            });
        if (UploadStatusEnum.SUCCESS.equals(result.getUploadStatus())) {
            UploadInfoChangeCondition condition = buildUploadInfoConditionByResult(result, request.getRegisterId());
            int affect = asyncDownloadRecordMapper.changeUploadInfo(condition);
            if (affect > 0) {
                return result;
            }
        }
        return null;
    }

    private boolean matchCacheKey(List<String> cacheKeyList, Map<String, Object> exportParamMap,
        AsyncDownloadRecord downloadRecord) {
        if (CollectionUtils.isEmpty(cacheKeyList)) {
            return true;
        }

        BaseExecuteParam executeParam = JSONUtil.parseObject(downloadRecord.getExecuteParam(), BaseExecuteParam.class);
        if (Objects.isNull(executeParam)) {
            if (MapUtils.isEmpty(exportParamMap)) {
                return true;
            }
        } else if (Objects.equals(executeParam.getOtherMap(), exportParamMap)) {
            return true;
        }
        StandardEvaluationContext oriEvaluationContext = new StandardEvaluationContext();
        oriEvaluationContext
            .setVariables(Objects.nonNull(executeParam) ? executeParam.getOtherMap() : Collections.emptyMap());

        StandardEvaluationContext tarEvaluationContext = new StandardEvaluationContext();
        tarEvaluationContext.setVariables(exportParamMap);

        boolean matchedKey = true;
        try {
            for (String cacheKey : cacheKeyList) {
                SpelExpressionParser expressionParser = new SpelExpressionParser();
                Expression expression = expressionParser.parseExpression(cacheKey);
                Object oriValue = expression.getValue(oriEvaluationContext);
                Object targetValue = expression.getValue(tarEvaluationContext);
                if (!Objects.equals(oriValue, targetValue)) {
                    matchedKey = false;
                }
            }
        } catch (Exception ex) {
            log.error(
                "[LocalDownloadStorageServiceImpl#matchCacheKey] parse matchKey error! cacheKey:{},exportParam:{},downloadRecord:{}",
                cacheKeyList, exportParamMap, downloadRecord, ex);
            matchedKey = false;
        }
        return matchedKey;
    }

    private ExportResult convertRecord(AsyncDownloadRecord downloadRecord, Long registerId) {
        ExportResult exportResult = new ExportResult();
        exportResult.setRegisterId(registerId);
        exportResult.setUploadStatus(downloadRecord.getUploadStatus());
        exportResult.setFileSystem(downloadRecord.getFileSystem());
        exportResult.setFileUrl(downloadRecord.getFileUrl());
        exportResult.setErrorMsg(downloadRecord.getErrorMsg());
        return exportResult;
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, DataExecuteErrorCode.NOT_EXIST_ERROR);
        UploadInfoChangeCondition condition = buildUploadInfoChangeCondition(request, downloadRecord);
        int affect = asyncDownloadRecordMapper.changeUploadInfo(condition);
        Asserts.isTrue(affect > 0, DataExecuteErrorCode.UPDATE_ERROR);
    }

    private UploadInfoChangeCondition buildUploadInfoChangeCondition(UploadCallbackRequest request,
        AsyncDownloadRecord downloadRecord) {
        UploadInfoChangeCondition uploadInfoChangeCondition = new UploadInfoChangeCondition();
        uploadInfoChangeCondition.setId(downloadRecord.getId());
        uploadInfoChangeCondition.setUploadStatus(request.getUploadStatus());
        uploadInfoChangeCondition.setFileUrl(request.getFileUrl());
        uploadInfoChangeCondition.setFileSystem(request.getSystem());
        uploadInfoChangeCondition.setErrorMsg(request.getErrorMsg());
        uploadInfoChangeCondition.setLastExecuteTime(new Date());
        uploadInfoChangeCondition.setInvalidTime(new Date());
        return uploadInfoChangeCondition;
    }

    private UploadInfoChangeCondition buildUploadInfoConditionByResult(ExportResult exportResult,
        Long registerId) {
        UploadInfoChangeCondition uploadInfoChangeCondition = new UploadInfoChangeCondition();
        uploadInfoChangeCondition.setId(registerId);
        uploadInfoChangeCondition.setUploadStatus(UploadStatusEnum.SUCCESS);
        uploadInfoChangeCondition.setFileUrl(exportResult.getFileUrl());
        uploadInfoChangeCondition.setFileSystem(exportResult.getFileSystem());
        uploadInfoChangeCondition.setErrorMsg(StringUtils.EMPTY);
        uploadInfoChangeCondition.setLastExecuteTime(new Date());
        uploadInfoChangeCondition.setInvalidTime(new Date());
        return uploadInfoChangeCondition;
    }

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        AsyncDownloadTask downloadTask = asyncDownloadTaskMapper
            .selectByDownloadCode(downloadRequest.getDownloadCode(), downloadRequest.getAppId());
        if (Objects.isNull(downloadTask)) {
            throw new EasyFileException(DownloadStorageErrorCode.DOWNLOAD_TASK_NOT_EXIST);
        }
        AsyncDownloadRecord downloadRecord = buildRegisterDefaultDownloadRecord(downloadRequest, downloadTask);
        int affect = asyncDownloadRecordMapper.insertSelective(downloadRecord);
        Asserts.isTrue(affect > 0, DataExecuteErrorCode.INSERT_ERROR);
        return downloadRecord.getId();
    }

    private AsyncDownloadRecord buildRegisterDefaultDownloadRecord(RegisterDownloadRequest request,
        AsyncDownloadTask downloadTask) {
        AsyncDownloadRecord downloadRecord = new AsyncDownloadRecord();
        downloadRecord.setDownloadTaskId(downloadTask.getId());
        downloadRecord.setAppId(request.getAppId());
        downloadRecord.setDownloadCode(request.getDownloadCode());
        downloadRecord.setUploadStatus(UploadStatusEnum.NONE);
        downloadRecord.setFileUrl(StringUtils.EMPTY);
        downloadRecord.setFileSystem(Constants.NONE_FILE_SYSTEM);
        downloadRecord.setDownloadOperateBy(
            Objects.nonNull(request.getNotifier()) ? request.getNotifier().getUserBy() : StringUtils.EMPTY);
        downloadRecord.setDownloadOperateName(
            Objects.nonNull(request.getNotifier()) ? request.getNotifier().getUserName() : StringUtils.EMPTY);
        downloadRecord.setRemark(request.getExportRemark());
        downloadRecord.setNotifyEnableStatus(Boolean.TRUE.equals(request.getEnableNotify())
            ? EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
        downloadRecord.setNotifyEmail(
            Objects.nonNull(request.getNotifier()) ? request.getNotifier().getEmail() : StringUtils.EMPTY);
        downloadRecord.setMaxServerRetry(request.getMaxServerRetry());
        downloadRecord.setCurrentRetry(0);
        downloadRecord.setExecuteParam(JSONUtil.toJson(request));
        downloadRecord.setErrorMsg(StringUtils.EMPTY);
        downloadRecord.setLastExecuteTime(new Date());
        downloadRecord.setInvalidTime(new Date());
        downloadRecord.setDownloadNum(0);
        downloadRecord.setExecuteProcess(0);
        downloadRecord.setVersion(Constants.DATA_INIT_VERSION);
        downloadRecord.setCreateTime(new Date());
        downloadRecord.setUpdateTime(new Date());
        downloadRecord.setCreateBy(Constants.SYSTEM_USER);
        downloadRecord.setUpdateBy(Constants.SYSTEM_USER);
        return downloadRecord;
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        if (Objects.isNull(request) || MapUtils.isEmpty(request.getDownloadCodeMap())) {
            return;
        }
        Map<String, AsyncDownloadTask> downloadTaskMap = buildDefaultAsyncDownloadTask(request);

        List<AsyncDownloadTask> downloadTaskList = asyncDownloadTaskMapper
            .listByDownloadCode(CollectionUtils.newArrayList(downloadTaskMap.keySet()),
                Collections.singletonList(request.getAppId()));
        Map<String, AsyncDownloadTask> existDownloadTaskMap = downloadTaskList.stream()
            .collect(Collectors.toMap(AsyncDownloadTask::getTaskCode, Function.identity()));

        for (Entry<String, String> entry : request.getDownloadCodeMap().entrySet()) {
            AsyncDownloadTask existedAsyncDownloadTask = existDownloadTaskMap.get(entry.getKey());
            if (Objects.isNull(existedAsyncDownloadTask)) {
                // 新增下载任务
                asyncDownloadTaskMapper.insertSelective(downloadTaskMap.get(entry.getKey()));
            } else if (!existedAsyncDownloadTask.getTaskDesc().equals(entry.getValue())) {
                // 刷新任务描述
                asyncDownloadTaskMapper.refreshTaskDesc(existedAsyncDownloadTask.getId(), entry.getValue());
            }
        }
    }

    private Map<String, AsyncDownloadTask> buildDefaultAsyncDownloadTask(AutoTaskRegisterRequest request) {
        return request.getDownloadCodeMap().entrySet().stream().map(e -> {
            AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask();
            asyncDownloadTask.setTaskCode(e.getKey());
            asyncDownloadTask.setTaskDesc(e.getValue());
            asyncDownloadTask.setAppId(request.getAppId());
            asyncDownloadTask.setUnifiedAppId(request.getUnifiedAppId());
            asyncDownloadTask.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
            asyncDownloadTask.setLimitingStrategy(Constants.DEFAULT_LIMITING_STRATEGY);
            asyncDownloadTask.setVersion(Constants.DATA_INIT_VERSION);
            asyncDownloadTask.setCreateTime(new Date());
            asyncDownloadTask.setUpdateTime(new Date());
            asyncDownloadTask.setCreateBy(Constants.SYSTEM_USER);
            asyncDownloadTask.setUpdateBy(Constants.SYSTEM_USER);
            asyncDownloadTask.setIsDeleted(0L);
            return asyncDownloadTask;
        }).collect(Collectors.toMap(AsyncDownloadTask::getTaskCode, Function.identity()));
    }

    @Override
    public String download(DownloadRequest request) {
        log.info("[AsyncDownload##download]file download,request:{}", request);

        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, AsyncDownloadExceptionCode.DOWNLOAD_RECORD_NOT_EXIST);
        // 下载
        int download = asyncDownloadRecordMapper.download(request.getRegisterId(), UploadStatusEnum.SUCCESS);
        Asserts.isTrue(download > 0, DownloadStorageErrorCode.DOWNLOAD_TASK_NOT_DOWNLOAD_SUCCESS);
        FileUrlTransformer transformer = FileUrlTransformerSupport.get(downloadRecord.getFileSystem());
        if (Objects.nonNull(transformer)) {
            return transformer.transform(downloadRecord.getFileUrl());
        }
        return downloadRecord.getFileUrl();
    }

    @Override
    public CancelUploadResult cancelUpload(CancelUploadRequest request) {
        CancelUploadResult uploadResult = new CancelUploadResult();
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        if (Objects.isNull(downloadRecord)) {
            uploadResult.setCancelResult(false);
            uploadResult.setCancelMsg("任务不存在");
            return uploadResult;
        }
        if (downloadRecord.getUploadStatus() != UploadStatusEnum.NONE) {
            uploadResult.setCancelResult(false);
            uploadResult.setCancelMsg("已经执行开始执行无法撤销");
            return uploadResult;
        }
        int affect = asyncDownloadRecordMapper.refreshUploadStatus(request.getRegisterId(),
            UploadStatusEnum.NONE, UploadStatusEnum.CANCEL, request.getCancelBy());
        uploadResult.setCancelResult(affect > 0);
        uploadResult.setCancelMsg(affect > 0 ? "撤销成功" : "撤销失败");
        return uploadResult;
    }

    @Override
    public DownloadRequestInfo getRequestInfoByRegisterId(Long registerId) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(registerId);
        if (Objects.isNull(downloadRecord)) {
            return null;
        }
        RegisterDownloadRequest registerRequest = JSONUtil
            .parseObject(downloadRecord.getExecuteParam(), RegisterDownloadRequest.class);
        Asserts.notNull(registerRequest, CommonErrorCode.PARAM_ILLEGAL_ERROR);

        DownloadRequestInfo requestInfo = new DownloadRequestInfo();
        requestInfo.setRequestContext(convertBaseDownloadRequestContext(registerRequest));
        requestInfo.setDownloadCode(downloadRecord.getDownloadCode());
        return requestInfo;
    }

    private BaseDownloaderRequestContext convertBaseDownloadRequestContext(RegisterDownloadRequest registerRequest) {
        BaseDownloaderRequestContext baseDownloaderRequestContext = new BaseDownloaderRequestContext();
        baseDownloaderRequestContext.setNotifier(registerRequest.getNotifier());
        baseDownloaderRequestContext.setFileSuffix(registerRequest.getFileSuffix());
        baseDownloaderRequestContext.setExportRemark(registerRequest.getExportRemark());
        baseDownloaderRequestContext.setOtherMap(registerRequest.getOtherMap());
        return baseDownloaderRequestContext;
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, UploadStatusEnum uploadStatus) {
        if (executeProcess <= 0) {
            return;
        }
        asyncDownloadRecordMapper.refreshExecuteProcess(registerId, executeProcess, uploadStatus);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        asyncDownloadRecordMapper.resetExecuteProcess(registerId);
    }

    @Override
    public Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request) {

        List<String> appIdList = asyncDownloadTaskMapper.getByUnifiedAppId(request.getUnifiedAppId());
        if (CollectionUtils.isEmpty(appIdList)) {
            return PaginationUtils.empty(request.getPageNum(), request.getPageSize());
        }
        BaseRecordQueryCondition condition = new BaseRecordQueryCondition();
        if (CollectionUtils.isEmpty(request.getLimitedAppIdList())) {
            condition.setLimitedAppIdList(appIdList);
        } else {
            List<String> intersectionList = (List<String>) CollectionUtils
                .intersection(appIdList, request.getLimitedAppIdList());
            if (CollectionUtils.isEmpty(intersectionList)) {
                return PaginationUtils.empty(request.getPageNum(), request.getPageSize());
            }
            condition.setLimitedAppIdList(intersectionList);
        }
        condition.setDownloadCode(request.getDownloadCode());
        condition.setDownloadOperateBy(request.getDownloadOperateBy());
        condition.setStartCreateTime(request.getStartDownloadTime());
        condition.setEndCreateTime(request.getEndDownloadTime());
        condition.setStartOffset(
            Pair.of(PageUtil.startIndex(request.getPageNum(), request.getPageSize()), request.getPageSize()));

        int total = asyncDownloadRecordMapper.countByCondition(condition);
        if (total <= 0) {
            return PaginationUtils.empty(request.getPageNum(), request.getPageSize());
        }
        List<AsyncDownloadRecord> recordList = asyncDownloadRecordMapper.selectByCondition(condition);

        List<String> downloadCodeList = recordList.stream()
            .map(AsyncDownloadRecord::getDownloadCode)
            .filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(downloadCodeList)) {
            return PaginationUtils.empty(request.getPageNum(), request.getPageSize());
        }
        List<AsyncDownloadTask> downloadTaskList = asyncDownloadTaskMapper
            .listByDownloadCode(downloadCodeList, condition.getLimitedAppIdList());
        Map<Long, AsyncDownloadTask> taskMap = downloadTaskList.parallelStream()
            .collect(Collectors.toMap(AsyncDownloadTask::getId, Function.identity()));

        List<DownloadResult> resultList = recordList.stream()
            .map(e -> AsyncDownloadRecordConverter.convert(e, taskMap.get(e.getDownloadTaskId())))
            .collect(Collectors.toList());

        // 分页结果
        Pagination<DownloadResult> pagination = new Pagination<>();
        pagination.setModelList(resultList);
        pagination.setTotalRecords((long) total);
        pagination.setPage((long) request.getPageNum());
        pagination.setPageSize(Long.valueOf(request.getPageSize()));
        return pagination;
    }
}
