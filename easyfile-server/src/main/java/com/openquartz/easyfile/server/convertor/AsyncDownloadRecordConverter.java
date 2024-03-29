package com.openquartz.easyfile.server.convertor;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.server.entity.AsyncDownloadRecord;
import com.openquartz.easyfile.server.entity.AsyncDownloadTask;

/**
 * 转换器
 *
 * @author svnee
 **/
public final class AsyncDownloadRecordConverter {

    private AsyncDownloadRecordConverter() {
    }

    public static DownloadResult convert(AsyncDownloadRecord downloadRecord, AsyncDownloadTask task) {
        DownloadResult downloadResult = new DownloadResult();
        downloadResult.setDownloadCode(downloadRecord.getDownloadCode());
        downloadResult.setDownloadOperateBy(downloadRecord.getDownloadOperateBy());
        downloadResult.setDownloadOperateName(downloadRecord.getDownloadOperateName());
        downloadResult.setRegisterId(downloadRecord.getId());
        downloadResult.setUploadStatus(downloadRecord.getUploadStatus());
        downloadResult.setFileSystem(downloadRecord.getFileSystem());
        // 非失效链接时加入
        if (UploadStatusEnum.INVALID == downloadRecord.getUploadStatus()) {
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
