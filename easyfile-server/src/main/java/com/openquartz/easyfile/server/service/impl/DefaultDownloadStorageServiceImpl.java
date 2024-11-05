package com.openquartz.easyfile.server.service.impl;

import java.util.List;
import java.util.Locale;

import com.openquartz.easyfile.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.server.service.AsyncDownloadService;
import com.openquartz.easyfile.server.service.AsyncDownloadTaskService;
import com.openquartz.easyfile.storage.download.DownloadStorageService;

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
        return asyncDownloadService.getRequestInfoByRegisterId(registerId);
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

    @Override
    public List<AppTree> getAppTree() {
        return asyncDownloadService.getAppTree();
    }

    @Override
    public Locale getCurrentLocale(Long registerId) {
        String locale = asyncDownloadService.getLocale(registerId);
        if (StringUtils.isNotBlank(locale)) {
            return Locale.forLanguageTag(locale);
        }
        return null;
    }
}
