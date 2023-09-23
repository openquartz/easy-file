package com.openquartz.easyfile.storage.local.convertor;

import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncFileRecord;
import com.openquartz.easyfile.storage.local.entity.AsyncFileTask;

/**
 * 转换器
 *
 * @author svnee
 **/
public final class AsyncDownloadRecordConverter {

    private AsyncDownloadRecordConverter() {
    }

    public static DownloadResult convert(AsyncFileRecord downloadRecord, AsyncFileTask task) {
        DownloadResult downloadResult = new DownloadResult();
        downloadResult.setDownloadCode(downloadRecord.getExecutorCode());
        downloadResult.setDownloadOperateBy(downloadRecord.getOperateBy());
        downloadResult.setDownloadOperateName(downloadRecord.getOperateName());
        downloadResult.setRegisterId(downloadRecord.getId());
        downloadResult.setUploadStatus(downloadRecord.getHandleStatus());
        downloadResult.setFileSystem(downloadRecord.getFileSystem());
        // 非失效链接时加入
        if (HandleStatusEnum.INVALID == downloadRecord.getHandleStatus()) {
            downloadResult.setFileUrl(StringUtils.EMPTY);
            downloadResult.setFileName(StringUtils.EMPTY);
        } else {
            downloadResult.setFileUrl(downloadRecord.getFileUrl());
            downloadResult.setFileName(downloadRecord.getFileName());
        }
        downloadResult.setExecuteProcess(downloadRecord.getExecuteProcess());
        downloadResult.setExportTime(downloadRecord.getCreateTime());
        downloadResult.setLastExecuteTime(downloadRecord.getLastExecuteTime());
        downloadResult.setInvalidTime(downloadRecord.getInvalidTime());
        downloadResult.setDownloadNum(downloadRecord.getDownloadNum());
        downloadResult.setErrorMsg(downloadRecord.getErrorMsg());
        downloadResult.setDownloadCodeDesc(task.getTaskDesc());
        downloadResult.setUpdateBy(downloadRecord.getUpdateBy());
        return downloadResult;
    }
}
