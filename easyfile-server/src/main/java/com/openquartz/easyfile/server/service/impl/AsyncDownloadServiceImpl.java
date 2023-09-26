package com.openquartz.easyfile.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.server.convertor.AsyncDownloadRecordConverter;
import com.openquartz.easyfile.server.entity.AsyncFileTask;
import com.openquartz.easyfile.server.service.executor.ExportLimitingExecutor;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.openquartz.easyfile.common.bean.BaseExecuteParam;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.CommonErrorCode;
import com.openquartz.easyfile.common.exception.DataExecuteErrorCode;
import com.openquartz.easyfile.common.exception.ExpandExecutorErrorCode;
import com.openquartz.easyfile.common.file.FileUrlTransformer;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.common.util.PaginationUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.server.config.BizConfig;
import com.openquartz.easyfile.server.entity.AsyncFileAppEntity;
import com.openquartz.easyfile.server.entity.AsyncFileRecord;
import com.openquartz.easyfile.server.exception.AsyncDownloadExceptionCode;
import com.openquartz.easyfile.server.mapper.AsyncFileRecordMapper;
import com.openquartz.easyfile.server.mapper.AsyncFileTaskMapper;
import com.openquartz.easyfile.server.mapper.condition.BaseRecordQueryCondition;
import com.openquartz.easyfile.server.mapper.condition.UploadInfoChangeCondition;
import com.openquartz.easyfile.server.notify.NotifyMessageTemplate;
import com.openquartz.easyfile.server.service.AsyncDownloadService;
import com.openquartz.easyfile.server.service.NotifyService;
import com.openquartz.easyfile.common.util.DbUtils;

/**
 * 注册下载 服务
 *
 * @author svnee
 */
@Slf4j
@Service
public class AsyncDownloadServiceImpl implements AsyncDownloadService, BeanPostProcessor {

    private final AsyncFileTaskMapper asyncFileTaskMapper;
    private final AsyncFileRecordMapper asyncFileRecordMapper;
    private final NotifyService notifyService;
    private final BizConfig bizConfig;

    /**
     * 限流导出执行器
     */
    private final Map<String, ExportLimitingExecutor> exportLimitingExecutorMap = new ConcurrentHashMap<>();
    /**
     * 文件转换器
     */
    private final Map<String, FileUrlTransformer> fileUrlTransformerMap = new ConcurrentHashMap<>();

    public AsyncDownloadServiceImpl(AsyncFileTaskMapper
        asyncFileTaskMapper,
        AsyncFileRecordMapper asyncFileRecordMapper,
        NotifyService notifyService,
        BizConfig bizConfig) {

        this.asyncFileTaskMapper = asyncFileTaskMapper;
        this.asyncFileRecordMapper = asyncFileRecordMapper;
        this.notifyService = notifyService;
        this.bizConfig = bizConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDownloadRequest request) {

        log.info("[DownloadService#Register] request:{}", request);
        AsyncFileTask downloadTask = asyncFileTaskMapper
            .selectByDownloadCode(request.getDownloadCode(), request.getAppId());
        Asserts.notNull(downloadTask, DataExecuteErrorCode.NOT_EXIST_ERROR);
        Asserts.isTrue(EnableStatusEnum.ENABLE.getCode().equals(downloadTask.getEnableStatus()),
            AsyncDownloadExceptionCode.DOWNLOAD_TASK_IS_DISABLE);

        AsyncFileRecord downloadRecord = buildAsyncRecord(request, downloadTask);
        int insertNum = asyncFileRecordMapper.insertSelective(downloadRecord);
        DbUtils.checkInsertedOneRow(insertNum);
        return downloadRecord.getId();
    }

    private AsyncFileRecord buildAsyncRecord(RegisterDownloadRequest request, AsyncFileTask downloadTask) {
        AsyncFileRecord downloadRecord = new AsyncFileRecord();
        downloadRecord.setId(downloadTask.getId());
        downloadRecord.setTaskId(downloadTask.getId());
        downloadRecord.setAppId(request.getAppId());
        downloadRecord.setExecutorCode(request.getDownloadCode());
        downloadRecord.setHandleStatus(HandleStatusEnum.NONE);
        downloadRecord.setFileUrl(StringUtils.EMPTY);
        downloadRecord.setFileName(StringUtils.EMPTY);
        downloadRecord.setFileSystem(Constants.NONE_FILE_SYSTEM);
        downloadRecord.setOperateBy(request.getNotifier().getUserBy());
        downloadRecord.setOperateName(request.getNotifier().getUserName());
        downloadRecord.setRemark(request.getExportRemark());
        downloadRecord.setNotifyEnableStatus(Boolean.TRUE.equals(request.getEnableNotify()) ?
            EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
        downloadRecord.setNotifyEmail(request.getNotifier().getEmail());
        downloadRecord.setVersion(Constants.DATA_INIT_VERSION);
        downloadRecord.setExecuteProcess(0);
        downloadRecord.setMaxServerRetry(request.getMaxServerRetry());
        downloadRecord.setCurrentRetry(0);
        downloadRecord.setDownloadNum(0);
        downloadRecord.setErrorMsg(StringUtils.EMPTY);
        Date now = new Date();
        downloadRecord.setLastExecuteTime(now);

        downloadRecord.setCreateTime(now);
        downloadRecord.setUpdateTime(now);
        downloadRecord.setCreateBy(request.getNotifier().getUserBy());
        downloadRecord.setUpdateBy(request.getNotifier().getUserBy());

        BaseExecuteParam param = new BaseExecuteParam();
        param.setDownloadCode(request.getDownloadCode());
        param.setFileSuffix(request.getFileSuffix());
        param.setOtherMap(request.getOtherMap());
        downloadRecord.setExecuteParam(JSONUtil.toClassJson(request));
        return downloadRecord;
    }

    @Override
    public boolean limiting(ExportLimitingRequest request) {
        AsyncFileTask downloadTask = asyncFileTaskMapper
            .selectByDownloadCode(request.getDownloadCode(), request.getAppId());
        Asserts.notNull(downloadTask, AsyncDownloadExceptionCode.DOWNLOAD_TASK_IS_DISABLE);

        ExportLimitingExecutor serviceProvider = exportLimitingExecutorMap.get(downloadTask.getLimitingStrategy());
        if (Objects.nonNull(serviceProvider)) {
            serviceProvider.limit(request);
        } else {
            log.error("[limit#downloadCode:{},strategy:{}]没有对应的策略!appId:{},request:{}", request.getDownloadCode(),
                downloadTask.getLimitingStrategy(),
                request.getAppId(), request);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadCallback(UploadCallbackRequest request) {

        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, DataExecuteErrorCode.NOT_EXIST_ERROR);
        // 如果是成功直接过滤掉
        if (downloadRecord.getHandleStatus() == HandleStatusEnum.SUCCESS) {
            return;
        }
        if (request.getUploadStatus() == HandleStatusEnum.SUCCESS) {
            // 直接更新执行成功信息
            UploadInfoChangeCondition condition = new UploadInfoChangeCondition();
            condition.setId(downloadRecord.getId());
            condition.setFileSystem(request.getSystem());
            condition.setUploadStatus(HandleStatusEnum.SUCCESS);
            condition.setFileUrl(request.getFileUrl());
            condition.setFileName(request.getFileName());
            condition.setLastExecuteTime(new Date());
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8));
            calendar.add(Calendar.HOUR, bizConfig.getFileInvalidTimeMap().getOrDefault(request.getSystem(), 100));
            condition.setInvalidTime(calendar.getTime());
            int changeNum = asyncFileRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRow(changeNum);

            // 如果需要执行通知,则执行通知
            if (downloadRecord.getNotifyEnableStatus().equals(EnableStatusEnum.ENABLE.getCode())) {
                AsyncFileTask task = asyncFileTaskMapper.findById(downloadRecord.getTaskId());

                Notifier notifier = new Notifier();
                notifier.setUserBy(downloadRecord.getOperateBy());
                notifier.setUserName(downloadRecord.getOperateName());
                notifier.setEmail(downloadRecord.getNotifyEmail());

                NotifyMessageTemplate template = new NotifyMessageTemplate();
                template.setContent(downloadRecord.getFileUrl());
                template.setTitle(task.getTaskDesc());
                template.setRemark(downloadRecord.getRemark());
                template.setRegisterId(downloadRecord.getId());
                template.setDownloadTime(downloadRecord.getCreateTime());
                template.setDownloadFinishedTime(downloadRecord.getLastExecuteTime());
                template.setErrorMsg(request.getErrorMsg());
                template.setUploadStatus(downloadRecord.getHandleStatus());
                notifyService.notify(notifier, template);
            }
        }
        // 如果还未上传,则执行初始化上传信息
        if (!downloadRecord.isUploadComplete()) {
            UploadInfoChangeCondition condition = new UploadInfoChangeCondition();
            condition.setId(downloadRecord.getId());
            condition.setUploadStatus(request.getUploadStatus());
            condition.setErrorMsg(request.getErrorMsg());
            condition.setLastExecuteTime(new Date());
            int changeNum = asyncFileRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRow(changeNum);
        }
    }

    @Override
    public boolean enableRunning(Long registerId) {
        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(registerId);
        if (Objects.isNull(downloadRecord) || downloadRecord.getHandleStatus() != HandleStatusEnum.NONE) {
            return false;
        }
        Boolean running = downloadRecord.getHandleStatus() == HandleStatusEnum.NONE;
        if (Boolean.TRUE.equals(running)) {
            int affect = asyncFileRecordMapper
                .refreshUploadStatus(registerId, HandleStatusEnum.NONE, HandleStatusEnum.EXECUTING,
                    downloadRecord.getUpdateBy());
            return affect > 0;
        }
        return false;
    }

    private Pagination<DownloadResult> getDownloadResultPagination(Integer pageNum, Integer pageSize,
        BaseRecordQueryCondition condition) {

        PageHelper.startPage(pageNum, pageSize);
        List<AsyncFileRecord> recordList = asyncFileRecordMapper.selectByCondition(condition);
        PageInfo<AsyncFileRecord> pageInfo = PageInfo.of(recordList);

        List<String> downloadCodeList = pageInfo.getList().parallelStream()
            .map(AsyncFileRecord::getExecutorCode)
            .filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(downloadCodeList)) {
            return PaginationUtils.empty(pageNum, pageSize);
        }
        List<AsyncFileTask> downloadTaskList = asyncFileTaskMapper
            .listByDownloadCode(downloadCodeList, condition.getLimitedAppIdList());
        Map<Long, AsyncFileTask> taskMap = downloadTaskList.parallelStream()
            .collect(Collectors.toMap(AsyncFileTask::getId, Function.identity()));

        List<DownloadResult> resultList = pageInfo.getList().stream()
            .map(e -> AsyncDownloadRecordConverter.convert(e, taskMap.get(e.getTaskId())))
            .collect(Collectors.toList());

        // 分页结果
        Pagination<DownloadResult> pagination = new Pagination<>();
        pagination.setModelList(resultList);
        pagination.setTotalRecords(pageInfo.getTotal());
        pagination.setPage((long) pageInfo.getPageNum());
        pagination.setPageSize(Long.valueOf(pageSize));
        return pagination;
    }

    @Override
    public Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request) {
        List<String> appIdList = asyncFileTaskMapper.getByUnifiedAppId(request.getUnifiedAppId());
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

        return getDownloadResultPagination(request.getPageNum(), request.getPageSize(), condition);
    }

    @Override
    public List<String> loadAllAppId() {
        List<String> appIdList = asyncFileTaskMapper.selectAllAppId();
        return appIdList.stream().filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());
    }

    @Override
    public void invalid() {
        BaseRecordQueryCondition condition = new BaseRecordQueryCondition();
        condition.setUploadStatus(HandleStatusEnum.SUCCESS);
        condition.setMaxInvalidTime(new Date());
        condition.setMaxLimit(500);
        List<AsyncFileRecord> recordList = asyncFileRecordMapper.selectByCondition(condition);
        for (AsyncFileRecord downloadRecord : recordList) {
            asyncFileRecordMapper
                .refreshUploadStatus(downloadRecord.getId(), HandleStatusEnum.SUCCESS, HandleStatusEnum.INVALID,
                    downloadRecord.getUpdateBy());
        }
    }

    @Override
    public DownloadUrlResult download(DownloadRequest request) {

        log.info("[AsyncDownload]#file download,request:{}", request);

        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, AsyncDownloadExceptionCode.DOWNLOAD_RECORD_NOT_EXIST);
        Asserts.isTrue(downloadRecord.getHandleStatus() == HandleStatusEnum.SUCCESS,
            AsyncDownloadExceptionCode.DOWNLOAD_STATUS_NOT_SUPPORT);

        // 下载
        asyncFileRecordMapper.download(request.getRegisterId(), HandleStatusEnum.SUCCESS);

        FileUrlTransformer transformer = fileUrlTransformerMap.get(downloadRecord.getFileSystem());

        String url = downloadRecord.getFileUrl();
        if (Objects.nonNull(transformer)) {
            url = transformer.transform(downloadRecord.getFileUrl());
        }

        DownloadUrlResult downloadUrlResult = new DownloadUrlResult();
        downloadUrlResult.setFileSystem(downloadRecord.getFileSystem());
        downloadUrlResult.setFileName(downloadRecord.getFileName());
        downloadUrlResult.setUrl(url);
        return downloadUrlResult;
    }

    @Override
    public CancelUploadResult cancel(CancelUploadRequest request) {
        CancelUploadResult uploadResult = new CancelUploadResult();
        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(request.getRegisterId());
        if (Objects.isNull(downloadRecord)) {
            uploadResult.setCancelResult(false);
            uploadResult.setCancelMsg("任务不存在");
            return uploadResult;
        }
        if (downloadRecord.getHandleStatus() != HandleStatusEnum.NONE) {
            uploadResult.setCancelResult(false);
            uploadResult.setCancelMsg("已经执行开始执行无法撤销");
            return uploadResult;
        }
        int affect = asyncFileRecordMapper.refreshUploadStatus(request.getRegisterId(),
            HandleStatusEnum.NONE, HandleStatusEnum.CANCEL, request.getCancelBy());
        uploadResult.setCancelResult(affect > 0);
        uploadResult.setCancelMsg(affect > 0 ? "撤销成功" : "撤销失败");
        return uploadResult;
    }

    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName)
        throws BeansException {
        if (Objects.nonNull(bean) && bean instanceof ExportLimitingExecutor) {
            ExportLimitingExecutor executor = (ExportLimitingExecutor) bean;
            ExportLimitingExecutor existExecutor = exportLimitingExecutorMap.putIfAbsent(executor.strategy(), executor);
            Asserts.isTrue(existExecutor == null || executor == existExecutor,
                ExpandExecutorErrorCode.LIMITING_STRATEGY_EXECUTOR_EXIST_ERROR,
                executor.strategy());
        }
        if (Objects.nonNull(bean) && bean instanceof FileUrlTransformer) {
            FileUrlTransformer transformer = (FileUrlTransformer) bean;
            FileUrlTransformer fileUrlTransformer = fileUrlTransformerMap
                .putIfAbsent(transformer.fileSystem(), transformer);
            Asserts.isTrue(transformer == fileUrlTransformer,
                ExpandExecutorErrorCode.LIMITING_STRATEGY_EXECUTOR_EXIST_ERROR,
                transformer.fileSystem());
            fileUrlTransformerMap.put(fileUrlTransformer.fileSystem(), transformer);
        }
        return bean;
    }

    @Override
    public ExportResult loadingExportCacheResult(LoadingExportCacheRequest request) {

        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, AsyncDownloadExceptionCode.DOWNLOAD_RECORD_NOT_EXIST);

        List<AsyncFileRecord> downloadRecordList = asyncFileRecordMapper
            .listByTaskIdAndStatus(downloadRecord.getTaskId(), HandleStatusEnum.SUCCESS, 500);

        ExportResult result = Optional.ofNullable(downloadRecordList)
            .orElse(Collections.emptyList())
            .stream()
            .filter(e -> matchCacheKey(request.getCacheKeyList(), request.getExportParamMap(), e))
            .findFirst().map(e -> convertRecord(e, request.getRegisterId()))
            .orElseGet(() -> {
                ExportResult exportResult = new ExportResult();
                exportResult.setRegisterId(request.getRegisterId());
                exportResult.setUploadStatus(HandleStatusEnum.FAIL);
                exportResult.setFileName(StringUtils.EMPTY);
                exportResult.setFileSystem(Constants.NONE_FILE_SYSTEM);
                return exportResult;
            });

        if (HandleStatusEnum.SUCCESS.equals(result.getUploadStatus())) {
            // 刷新
            UploadInfoChangeCondition condition = buildUploadInfoConditionByResult(result, request.getRegisterId());
            int affect = asyncFileRecordMapper.changeUploadInfo(condition);
            if (affect > 0) {
                return result;
            }
        }
        return null;
    }

    private boolean matchCacheKey(List<String> cacheKeyList, Map<String, Object> exportParamMap,
        AsyncFileRecord downloadRecord) {
        if (CollectionUtils.isEmpty(cacheKeyList)) {
            return true;
        }

        BaseExecuteParam executeParam = JSONUtil
            .parseClassObject(downloadRecord.getExecuteParam(), BaseExecuteParam.class);
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
                "[AsyncDownloadServiceImpl#matchCacheKey] parse matchKey error! cacheKey:{},exportParam:{},downloadRecord:{}",
                cacheKeyList, exportParamMap, downloadRecord, ex);
            matchedKey = false;
        }
        return matchedKey;
    }

    private ExportResult convertRecord(AsyncFileRecord downloadRecord, Long registerId) {
        ExportResult exportResult = new ExportResult();
        exportResult.setRegisterId(registerId);
        exportResult.setUploadStatus(downloadRecord.getHandleStatus());
        exportResult.setFileSystem(downloadRecord.getFileSystem());
        exportResult.setFileName(downloadRecord.getFileName());
        exportResult.setFileUrl(downloadRecord.getFileUrl());
        exportResult.setErrorMsg(downloadRecord.getErrorMsg());
        return exportResult;
    }

    private UploadInfoChangeCondition buildUploadInfoConditionByResult(ExportResult exportResult,
        Long registerId) {
        UploadInfoChangeCondition uploadInfoChangeCondition = new UploadInfoChangeCondition();
        uploadInfoChangeCondition.setId(registerId);
        uploadInfoChangeCondition.setUploadStatus(HandleStatusEnum.SUCCESS);
        uploadInfoChangeCondition.setFileUrl(exportResult.getFileUrl());
        uploadInfoChangeCondition.setFileName(exportResult.getFileName());
        uploadInfoChangeCondition.setFileSystem(exportResult.getFileSystem());
        uploadInfoChangeCondition.setErrorMsg(StringUtils.EMPTY);
        uploadInfoChangeCondition.setLastExecuteTime(new Date());
        uploadInfoChangeCondition.setInvalidTime(new Date());
        return uploadInfoChangeCondition;
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        asyncFileRecordMapper.resetExecuteProcess(registerId);
    }

    @Override
    public void refreshExecuteProcess(Long registerId, Integer executeProcess,
        HandleStatusEnum nextUploadStatus) {
        asyncFileRecordMapper.refreshExecuteProcess(registerId, executeProcess, nextUploadStatus);
    }

    @Override
    public DownloadRequestInfo getRequestInfoByRegisterId(Long registerId) {
        AsyncFileRecord downloadRecord = asyncFileRecordMapper.findById(registerId);
        if (Objects.isNull(downloadRecord)) {
            return null;
        }
        RegisterDownloadRequest registerRequest = JSONUtil
            .parseClassObject(downloadRecord.getExecuteParam(), RegisterDownloadRequest.class);
        Asserts.notNull(registerRequest, CommonErrorCode.PARAM_ILLEGAL_ERROR);

        DownloadRequestInfo requestInfo = new DownloadRequestInfo();
        requestInfo.setRequestContext(convertBaseDownloadRequestContext(registerRequest));
        requestInfo.setDownloadCode(downloadRecord.getExecutorCode());
        return requestInfo;
    }

    private BaseExportRequestContext convertBaseDownloadRequestContext(RegisterDownloadRequest registerRequest) {
        BaseExportRequestContext baseExportRequestContext = new BaseExportRequestContext();
        baseExportRequestContext.setNotifier(registerRequest.getNotifier());
        baseExportRequestContext.setFileSuffix(registerRequest.getFileSuffix());
        baseExportRequestContext.setExportRemark(registerRequest.getExportRemark());
        baseExportRequestContext.setOtherMap(registerRequest.getOtherMap());
        return baseExportRequestContext;
    }

    @Override
    public List<AppTree> getAppTree() {
        List<AsyncFileAppEntity> appEntityList = asyncFileTaskMapper.getAppTree();
        return appEntityList.stream()
            .collect(Collectors.groupingBy(AsyncFileAppEntity::getUnifiedAppId,
                Collectors.mapping(AsyncFileAppEntity::getAppId, Collectors.toList())))
            .entrySet()
            .stream()
            .map(e -> new AppTree(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
