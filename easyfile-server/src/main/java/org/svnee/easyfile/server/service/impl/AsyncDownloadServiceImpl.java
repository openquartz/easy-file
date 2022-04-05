package org.svnee.easyfile.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.svnee.easyfile.common.bean.BaseExecuteParam;
import org.svnee.easyfile.common.bean.Notifier;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.EnableStatusEnum;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.DataExecuteErrorCode;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.server.common.spi.SpiSupport;
import org.svnee.easyfile.server.config.BizConfig;
import org.svnee.easyfile.server.convertor.AsyncDownloadRecordConverter;
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
import org.svnee.easyfile.server.service.executor.LimitingConstants;
import org.svnee.easyfile.server.utils.DbUtils;
import org.svnee.easyfile.server.utils.PaginationUtils;

/**
 * 注册下载 服务
 *
 * @author svnee
 */
@Service
@RequiredArgsConstructor
public class AsyncDownloadServiceImpl implements AsyncDownloadService {

    private static final Logger log = LoggerFactory.getLogger(AsyncDownloadServiceImpl.class);

    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    private final AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    private final NotifyService notifyService;
    private final BizConfig bizConfig;

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
        DbUtils.checkInsertedOneRows(insertNum);
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
        downloadRecord.setFileSystem(Constants.NONE_FILE_SYSTEM);
        downloadRecord.setDownloadOperateBy(request.getNotifier().getUserBy());
        downloadRecord.setDownloadOperateName(request.getNotifier().getUserName());
        downloadRecord.setRemark(request.getRemark());
        downloadRecord.setNotifyEnableStatus(Boolean.TRUE.equals(request.getEnableNotify()) ?
            EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
        downloadRecord.setNotifyEmail(request.getNotifier().getEmail());
        downloadRecord.setVersion(Constants.DATA_INIT_VERSION);
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
        if (Objects.isNull(downloadTask)) {
            return true;
        }

        ExportLimitingExecutor serviceProvider = SpiSupport.getServiceProvider(ExportLimitingExecutor.class,
            LimitingConstants.LIMITING_PREFIX + downloadTask.getLimitingStrategy());
        if (Objects.nonNull(serviceProvider)) {
            return serviceProvider.limit(request);
        } else {
            log.error("[limit#downloadCode:{}]没有对应的策略!appId:{},request:{}", request.getDownloadCode(),
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
            condition.setLastExecuteTime(new Date());
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8));
            calendar.add(Calendar.HOUR, bizConfig.getFileInvalidTimeMap().getOrDefault(request.getSystem(), 100));
            condition.setInvalidTime(calendar.getTime());
            int changeNum = asyncDownloadRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRows(changeNum);

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
                notifyService.notify(notifier, template);
            }
        }
        // 如果还未上传,则执行初始化上传信息
        if (downloadRecord.getUploadStatus() == UploadStatusEnum.NONE) {
            UploadInfoChangeCondition condition = new UploadInfoChangeCondition();
            condition.setId(downloadRecord.getId());
            condition.setUploadStatus(request.getUploadStatus());
            condition.setErrorMsg(request.getErrorMsg());
            condition.setLastExecuteTime(new Date());
            int changeNum = asyncDownloadRecordMapper.changeUploadInfo(condition);
            DbUtils.checkUpdatedOneRows(changeNum);
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
        Pagination<DownloadResult> pagination = new Pagination<>();
        pagination.setModels(resultList);
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
    public void download(DownloadRequest request) {
        log.info("[AsyncDownload]#file download,request:{}", request);
        asyncDownloadRecordMapper.download(request.getRegisterId(), UploadStatusEnum.SUCCESS);
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
}
