package org.svnee.easyfile.server.convertor;

import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.server.entity.AsyncDownloadRecord;
import org.svnee.easyfile.server.entity.AsyncDownloadTask;

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
        } else {
            downloadResult.setFileUrl(downloadRecord.getFileUrl());
        }
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
