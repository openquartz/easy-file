package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.common.bean.ImportRecordInfo;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.request.ImportCallbackRequest;
import com.openquartz.easyfile.common.request.RegisterImportRequest;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTask;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.storage.local.exception.ImportStorageErrorCode;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTaskMapper;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * LocalImportStorageServiceImpl
 *
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class LocalImportStorageServiceImpl implements ImportStorageService {

    private final AsyncImportTaskMapper asyncImportTaskMapper;
    private final AsyncImportRecordMapper asyncImportRecordMapper;

    @Override
    public Long register(RegisterImportRequest request) {
        AsyncImportTask importTask = asyncImportTaskMapper.selectByTaskCode(request.getImportCode(), request.getAppId());
        if (Objects.isNull(importTask)) {
            importTask = new AsyncImportTask();
            importTask.setTaskCode(request.getImportCode());
            importTask.setTaskDesc(request.getRemark());
            importTask.setAppId(request.getAppId());
            importTask.setUnifiedAppId(request.getAppId());
            importTask.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
            importTask.setLimitingStrategy(StringUtils.EMPTY);
            importTask.setVersion(1);
            importTask.setCreateTime(new Date());
            importTask.setUpdateTime(new Date());
            importTask.setCreateBy(request.getNotifier().getUserBy());
            importTask.setUpdateBy(request.getNotifier().getUserBy());
            importTask.setIsDeleted(0L);
            asyncImportTaskMapper.insertSelective(importTask);
        }

        AsyncImportRecord record = new AsyncImportRecord();
        record.setImportTaskId(importTask.getId());
        record.setAppId(request.getAppId());
        record.setImportCode(request.getImportCode());
        record.setUploadStatus(UploadStatusEnum.NONE);
        record.setFileUrl(request.getFileUrl());
        record.setFileName(getFileName(request.getFileUrl()));
        record.setFileSystem(Constants.NONE_FILE_SYSTEM);
        record.setImportOperateBy(request.getNotifier().getUserBy());
        record.setImportOperateName(request.getNotifier().getUserName());
        record.setRemark(request.getRemark());
        record.setNotifyEnableStatus(0);
        record.setNotifyEmail(request.getNotifier().getEmail());
        record.setMaxServerRetry(0);
        record.setCurrentRetry(0);
        record.setExecuteParam(JSONUtil.toJson(request));
        record.setErrorMsg(StringUtils.EMPTY);
        record.setLastExecuteTime(new Date());
        record.setInvalidTime(getInvalidTime());
        record.setExecuteProcess(0);
        record.setErrorFileUrl(StringUtils.EMPTY);
        record.setSuccessRows(0);
        record.setFailRows(0);
        record.setTotalRows(0);
        record.setVersion(1);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setCreateBy(request.getNotifier().getUserBy());
        record.setUpdateBy(request.getNotifier().getUserBy());
        record.setLocale(LocaleContextHolder.getLocale().toString());

        asyncImportRecordMapper.insertSelective(record);
        return record.getId();
    }

    @Override
    public void updateImportResult(ImportCallbackRequest request) {
        asyncImportRecordMapper.updateImportResult(request.getRegisterId(),
            request.getUploadStatus(),
            request.getErrorFileUrl(),
            request.getSuccessRows(),
            request.getFailRows(),
            request.getTotalRows());
    }

    @Override
    public boolean enableRunning(Long registerId) {
        AsyncImportRecord record = asyncImportRecordMapper.findById(registerId);
        Asserts.notNull(record, ImportStorageErrorCode.IMPORT_TASK_NOT_EXIST);
        if (record.getUploadStatus() != UploadStatusEnum.NONE) {
            return false;
        }
        int affected = asyncImportRecordMapper.updateUploadStatus(registerId, UploadStatusEnum.EXECUTING, record.getUpdateBy());
        return affected > 0;
    }

    @Override
    public ImportRecordInfo getImportRecord(Long registerId) {
        AsyncImportRecord record = asyncImportRecordMapper.findById(registerId);
        if (record == null) {
            return null;
        }
        ImportRecordInfo info = new ImportRecordInfo();
        info.setId(record.getId());
        info.setAppId(record.getAppId());
        info.setImportCode(record.getImportCode());
        info.setFileUrl(record.getFileUrl());
        info.setFileName(record.getFileName());
        return info;
    }

    private String getFileName(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        try {
            return new java.io.File(new java.net.URL(url).getPath()).getName();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    private Date getInvalidTime() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 30);
        return calendar.getTime();
    }

}
