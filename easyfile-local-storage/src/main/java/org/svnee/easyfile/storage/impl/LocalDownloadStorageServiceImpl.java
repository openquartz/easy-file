package org.svnee.easyfile.storage.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.EnableStatusEnum;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.exception.AssertUtil;
import org.svnee.easyfile.common.exception.DataExecuteErrorCode;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.entity.AsyncDownloadRecord;
import org.svnee.easyfile.storage.entity.AsyncDownloadTask;
import org.svnee.easyfile.storage.exception.DownloadStorageErrorCode;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.mapper.condition.UploadInfoChangeCondition;

/**
 * 本地下载存储
 *
 * @author svnee
 **/
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
        // todo 分布式锁-锁住共享资源
        //        String generateKey = lockKeyFactory.genKey(LockKeyFormatter.DOWNLOAD_RUNNING_LOCK, registerId);
        //        Boolean running = redisTemplate.opsForValue()
        //            .setIfAbsent(generateKey, "true", systemConfig.getDownloadRunningLockSeconds(), TimeUnit.SECONDS);
        boolean running = true;
        if (Boolean.TRUE.equals(running)) {
            int affect = asyncDownloadRecordMapper
                .refreshUploadStatus(registerId, UploadStatusEnum.NONE, UploadStatusEnum.EXECUTING,
                    downloadRecord.getUpdateBy());
            return affect > 0;
        }
        return Boolean.TRUE.equals(running);
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {
        AsyncDownloadRecord downloadRecord = asyncDownloadRecordMapper.findById(request.getRegisterId());
        AssertUtil.notNull(downloadRecord, DataExecuteErrorCode.NOT_EXIST_ERROR);
        UploadInfoChangeCondition condition = buildUploadInfoChangeCondition(request, downloadRecord);
        int affect = asyncDownloadRecordMapper.changeUploadInfo(condition);
        AssertUtil.isTrue(affect > 0, DataExecuteErrorCode.UPDATE_ERROR);
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

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        AsyncDownloadTask downloadTask = asyncDownloadTaskMapper
            .selectByDownloadCode(downloadRequest.getDownloadCode(), downloadRequest.getAppId());
        if (Objects.isNull(downloadTask)) {
            throw new EasyFileException(DownloadStorageErrorCode.DOWNLOAD_TASK_NOT_EXIST);
        }
        AsyncDownloadRecord downloadRecord = buildRegisterDefaultDownloadRecord(downloadRequest, downloadTask);
        int affect = asyncDownloadRecordMapper.insertSelective(downloadRecord);
        AssertUtil.isTrue(affect > 0, DataExecuteErrorCode.INSERT_ERROR);
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
        downloadRecord.setDownloadOperateBy(request.getNotifier().getUserBy());
        downloadRecord.setDownloadOperateName(request.getNotifier().getUserName());
        downloadRecord.setRemark(request.getRemark());
        downloadRecord.setNotifyEnableStatus(Boolean.TRUE.equals(request.getEnableNotify())
            ? EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
        downloadRecord.setNotifyEmail(request.getNotifier().getEmail());
        downloadRecord.setMaxServerRetry(request.getMaxServerRetry());
        downloadRecord.setCurrentRetry(0);
        downloadRecord.setExecuteParam(JSONUtil.toJson(request));
        downloadRecord.setErrorMsg(StringUtils.EMPTY);
        downloadRecord.setLastExecuteTime(new Date());
        downloadRecord.setInvalidTime(new Date());
        downloadRecord.setDownloadNum(0);
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

}
