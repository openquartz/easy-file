package org.svnee.easyfile.server.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.DownloadUrlResult;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.server.service.AsyncDownloadService;
import org.svnee.easyfile.server.service.AsyncDownloadTaskService;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * DefaultDownloadStorageServiceImpl
 *
 * @author svnee
 **/
@Service
@RequiredArgsConstructor
public class DefaultDownloadStorageServiceImpl implements DownloadStorageService {

    private final AsyncDownloadTaskService asyncDownloadTaskService;
    private final AsyncDownloadService asyncDownloadService;

    @Override
    public boolean enableRunning(Long registerId) {
        return asyncDownloadService.enableRunning(registerId);
    }

    @Override
    public ExportResult loadingCacheExportResult(LoadingExportCacheRequest request) {
        return asyncDownloadService.loadingExportCacheResult(request);
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {
        asyncDownloadService.uploadCallback(request);
    }

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        return asyncDownloadService.register(downloadRequest);
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        asyncDownloadTaskService.autoRegister(request);
    }

    @Override
    public DownloadUrlResult download(DownloadRequest request) {
        return asyncDownloadService.download(request);
    }

    @Override
    public CancelUploadResult cancelUpload(CancelUploadRequest request) {
        return asyncDownloadService.cancel(request);
    }

    @Override
    public DownloadRequestInfo getRequestInfoByRegisterId(Long registerId) {
        // TODO: 2023/3/9
        return null;
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, UploadStatusEnum nextUploadStatus) {
        asyncDownloadService.refreshExecuteProcess(registerId, executeProcess, nextUploadStatus);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        asyncDownloadService.resetExecuteProcess(registerId);
    }

    @Override
    public Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request) {
        return asyncDownloadService.listExportResult(request);
    }
}
