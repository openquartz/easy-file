package org.svnee.easyfile.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.BaseExecuteParam;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.bean.Notifier;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.EnableStatusEnum;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.CommonErrorCode;
import org.svnee.easyfile.common.exception.DataExecuteErrorCode;
import org.svnee.easyfile.common.exception.ExpandExecutorErrorCode;
import org.svnee.easyfile.common.file.FileUrlTransformer;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.AppTree;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.DownloadUrlResult;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.PaginationUtils;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.server.config.BizConfig;
import org.svnee.easyfile.server.convertor.AsyncDownloadRecordConverter;
import org.svnee.easyfile.server.entity.AsyncDownloadAppEntity;
import org.svnee.easyfile.server.entity.AsyncDownloadRecord;
import org.svnee.easyfile.server.entity.AsyncDownloadTask;
import org.svnee.easyfile.server.exception.AsyncDownloadExceptionCode;
import org.svnee.easyfile.server.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.server.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.server.mapper.condition.BaseRecordQueryCondition;
import org.svnee.easyfile.server.mapper.condition.UploadInfoChangeCondition;
import org.svnee.easyfile.server.notify.NotifyMessageTemplate;
import org.svnee.easyfile.server.service.AsyncDownloadService;
import org.svnee.easyfile.server.service.NotifyService;
import org.svnee.easyfile.server.service.executor.ExportLimitingExecutor;
import org.svnee.easyfile.server.utils.DbUtils;

/**
 * 注册下载 服务
 *
 * @author svnee
 */
@Slf4j
@Service
public class AsyncDownloadServiceImpl implements AsyncDownloadService, BeanPostProcessor {

    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    private final AsyncDownloadRecordMapper asyncDownloadRecordMapper;
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

    public AsyncDownloadServiceImpl(AsyncDownloadTaskMapper asyncDownloadTaskMapper,
        AsyncDownloadRecordMapper asyncDownloadRecordMapper, NotifyService notifyService,
        BizConfig bizConfig) {
        this.asyncDownloadTaskMapper = asyncDownloadTaskMapper;
        this.asyncDownloadRecordMapper = asyncDownloadRecordMapper;
        this.notifyService = notifyService;
        this.bizConfig = bizConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDownloadRequest request) {

        log.info("[DownloadService#Register] request:{}", request);
        AsyncDownloadTask downloadTask = asyncDownloadTaskMapper
            .selectByDownloadCode(request.getDownloadCode(), request.getAppId());
        Asserts.notNull(downloadTask, DataExecuteErrorCode.NOT_EXIST_ERROR);
        Asserts.isTrue(EnableStatusEnum.ENABLE.getCode().equals(downloadTask.getEnableStatus()),
            AsyncDownloadExceptionCode.DOWNLOAD_TASK_IS_DISABLE);

        AsyncDownloadRecord downloadRecord = buildAsyncRecord(request, downloadTask);
        int insertNum = asyncDownloadRecordMapper.insertSelective(downloadRecord);
        DbUtils.checkInsertedOneRow(insertNum);
        return downloadRecord.getId();
    }

    private AsyncDownloadRecord buildAsyncRecord(RegisterDownloadRequest request, AsyncDownloadTask downloadTask) {
        AsyncDownloadRecord downloadRecord = new AsyncDownloadRecord();
        downloadRecord.setId(downloadTask.getId());
        downloadRecord.setDownloadTaskId(downloadTask.getId());
        downloadRecord.setAppId(request.getAppId());
        downloadRecord.setDownloadCode(request.getDownloadCode());
        downloadRecord.setUploadStatus(UploadStatusEnum.NONE);
        downloadRecord.setFileUrl(StringUtils.EMPTY);
        downloadRecord.setFileName(StringUtils.EMPTY);
        downloadRecord.setFileSystem(Constants.NONE_FILE_SYSTEM);
        downloadRecord.setDownloadOperateBy(request.getNotifier().getUserBy());
        downloadRecord.setDownloadOperateName(request.getNotifier().getUserName());
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
        downloadRecord.setExecuteParam(JSONUtil.toJson(request));
        return downloadRecord;
    }

    @Override
    public boolean limiting(ExportLimitingRequest request) {
        AsyncDownloadTask downloadTask = asyncDownloadTaskMapper
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

        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, DataExecuteErrorCode.NOT_EXIST_ERROR);
        // 如果是成功直接过滤掉
        if (downloadRecord.getUploadStatus() == UploadStatusEnum.SUCCESS) {
            return;
        }
        if (request.getUploadStatus() == UploadStatusEnum.SUCCESS) {
            // 直接更新执行成功信息
            UploadInfoChangeCondition condition = new UploadInfoChangeCondition();
            condition.setId(downloadRecord.getId());
            condition.setFileSystem(request.getSystem());
            condition.setUploadStatus(UploadStatusEnum.SUCCESS);
            condition.setFileUrl(request.getFileUrl());
            condition.setFileName(request.getFileName());
            condition.setLastExecuteTime(new Date());
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8));
            calendar.add(Calendar.HOUR, bizConfig.getFileInvalidTimeMap().getOrDefault(request.getSystem(), 100));
            condition.setInvalidTime(calendar.getTime());
            int changeNum = asyncDownloadRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRow(changeNum);

            // 如果需要执行通知,则执行通知
            if (downloadRecord.getNotifyEnableStatus().equals(EnableStatusEnum.ENABLE.getCode())) {
                AsyncDownloadTask task = asyncDownloadTaskMapper.findById(downloadRecord.getDownloadTaskId());

                Notifier notifier = new Notifier();
                notifier.setUserBy(downloadRecord.getDownloadOperateBy());
                notifier.setUserName(downloadRecord.getDownloadOperateName());
                notifier.setEmail(downloadRecord.getNotifyEmail());

                NotifyMessageTemplate template = new NotifyMessageTemplate();
                template.setContent(downloadRecord.getFileUrl());
                template.setTitle(task.getTaskDesc());
                template.setRemark(downloadRecord.getRemark());
                template.setRegisterId(downloadRecord.getId());
                template.setDownloadTime(downloadRecord.getCreateTime());
                template.setDownloadFinishedTime(downloadRecord.getLastExecuteTime());
                template.setErrorMsg(request.getErrorMsg());
                template.setUploadStatus(downloadRecord.getUploadStatus());
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
            int changeNum = asyncDownloadRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRow(changeNum);
        }
    }

    @Override
    public boolean enableRunning(Long registerId) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(registerId);
        if (Objects.isNull(downloadRecord) || downloadRecord.getUploadStatus() != UploadStatusEnum.NONE) {
            return false;
        }
        Boolean running = downloadRecord.getUploadStatus() == UploadStatusEnum.NONE;
        if (Boolean.TRUE.equals(running)) {
            int affect = asyncDownloadRecordMapper
                .refreshUploadStatus(registerId, UploadStatusEnum.NONE, UploadStatusEnum.EXECUTING,
                    downloadRecord.getUpdateBy());
            return affect > 0;
        }
        return false;
    }

    private Pagination<DownloadResult> getDownloadResultPagination(Integer pageNum, Integer pageSize,
        BaseRecordQueryCondition condition) {

        PageHelper.startPage(pageNum, pageSize);
        List<AsyncDownloadRecord> recordList = asyncDownloadRecordMapper.selectByCondition(condition);
        PageInfo<AsyncDownloadRecord> pageInfo = PageInfo.of(recordList);

        List<String> downloadCodeList = pageInfo.getList().parallelStream()
            .map(AsyncDownloadRecord::getDownloadCode)
            .filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(downloadCodeList)) {
            return PaginationUtils.empty(pageNum, pageSize);
        }
        List<AsyncDownloadTask> downloadTaskList = asyncDownloadTaskMapper
            .listByDownloadCode(downloadCodeList, condition.getLimitedAppIdList());
        Map<Long, AsyncDownloadTask> taskMap = downloadTaskList.parallelStream()
            .collect(Collectors.toMap(AsyncDownloadTask::getId, Function.identity()));

        List<DownloadResult> resultList = pageInfo.getList().stream()
            .map(e -> AsyncDownloadRecordConverter.convert(e, taskMap.get(e.getDownloadTaskId())))
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

        return getDownloadResultPagination(request.getPageNum(), request.getPageSize(), condition);
    }

    @Override
    public List<String> loadAllAppId() {
        List<String> appIdList = asyncDownloadTaskMapper.selectAllAppId();
        return appIdList.stream().filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());
    }

    @Override
    public void invalid() {
        BaseRecordQueryCondition condition = new BaseRecordQueryCondition();
        condition.setUploadStatus(UploadStatusEnum.SUCCESS);
        condition.setMaxInvalidTime(new Date());
        condition.setMaxLimit(500);
        List<AsyncDownloadRecord> recordList = asyncDownloadRecordMapper.selectByCondition(condition);
        for (AsyncDownloadRecord downloadRecord : recordList) {
            asyncDownloadRecordMapper
                .refreshUploadStatus(downloadRecord.getId(), UploadStatusEnum.SUCCESS, UploadStatusEnum.INVALID,
                    downloadRecord.getUpdateBy());
        }
    }

    @Override
    public DownloadUrlResult download(DownloadRequest request) {

        log.info("[AsyncDownload]#file download,request:{}", request);

        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, AsyncDownloadExceptionCode.DOWNLOAD_RECORD_NOT_EXIST);
        Asserts.isTrue(downloadRecord.getUploadStatus() == UploadStatusEnum.SUCCESS,
            AsyncDownloadExceptionCode.DOWNLOAD_STATUS_NOT_SUPPORT);

        // 下载
        asyncDownloadRecordMapper.download(request.getRegisterId(), UploadStatusEnum.SUCCESS);

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

        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        Asserts.notNull(downloadRecord, AsyncDownloadExceptionCode.DOWNLOAD_RECORD_NOT_EXIST);

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
                exportResult.setFileName(StringUtils.EMPTY);
                exportResult.setFileSystem(Constants.NONE_FILE_SYSTEM);
                return exportResult;
            });

        if (UploadStatusEnum.SUCCESS.equals(result.getUploadStatus())) {
            // 刷新
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
                "[AsyncDownloadServiceImpl#matchCacheKey] parse matchKey error! cacheKey:{},exportParam:{},downloadRecord:{}",
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
        exportResult.setFileName(downloadRecord.getFileName());
        exportResult.setFileUrl(downloadRecord.getFileUrl());
        exportResult.setErrorMsg(downloadRecord.getErrorMsg());
        return exportResult;
    }

    private UploadInfoChangeCondition buildUploadInfoConditionByResult(ExportResult exportResult,
        Long registerId) {
        UploadInfoChangeCondition uploadInfoChangeCondition = new UploadInfoChangeCondition();
        uploadInfoChangeCondition.setId(registerId);
        uploadInfoChangeCondition.setUploadStatus(UploadStatusEnum.SUCCESS);
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
        asyncDownloadRecordMapper.resetExecuteProcess(registerId);
    }

    @Override
    public void refreshExecuteProcess(Long registerId, Integer executeProcess,
        UploadStatusEnum nextUploadStatus) {
        asyncDownloadRecordMapper.refreshExecuteProcess(registerId, executeProcess, nextUploadStatus);
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
    public List<AppTree> getAppTree() {
        List<AsyncDownloadAppEntity> appEntityList = asyncDownloadTaskMapper.getAppTree();
        return appEntityList.stream()
            .collect(Collectors.groupingBy(AsyncDownloadAppEntity::getUnifiedAppId,
                Collectors.mapping(AsyncDownloadAppEntity::getAppId, Collectors.toList())))
            .entrySet()
            .stream()
            .map(e -> new AppTree(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
