package com.openquartz.easyfile.storage.remote.impl;

import com.openquartz.easyfile.storage.remote.common.HttpAgent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RefreshExecuteProcessRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.common.util.json.TypeReference;
import com.openquartz.easyfile.storage.remote.EasyFileClient;

/**
 * Http 请求
 *
 * @author svnee
 **/
@Slf4j
public class HttpEasyFileClientImpl implements EasyFileClient {

    private final HttpAgent httpAgent;

    public HttpEasyFileClientImpl(HttpAgent httpAgent) {
        this.httpAgent = httpAgent;
    }

    @Override
    public ResponseResult<Long> register(RegisterDownloadRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_REGISTER_URL, request, Long.class);
    }

    @Override
    public ResponseResult<?> autoRegisterTask(AutoTaskRegisterRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.TASK_AUTO_REGISTER_URL, request);
    }

    @Override
    public ResponseResult<?> uploadCallback(UploadCallbackRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_CALLBACK_URL, request);
    }

    @Override
    public ResponseResult<?> limiting(ExportLimitingRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_LIMITING_URL, request);
    }

    @Override
    public ResponseResult<Boolean> enableRunning(EnableRunningRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_ENABLE_RUNNING_URL, request, Boolean.class);
    }

    @Override
    public ResponseResult<Pagination<DownloadResult>> listExportResult(ListDownloadResultRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_RECORD_URL, request,
            new TypeReference<ResponseResult<Pagination<DownloadResult>>>() {
            });
    }

    @Override
    public ResponseResult<DownloadUrlResult> download(DownloadRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_FILE_URL, request, DownloadUrlResult.class);
    }

    @Override
    public ResponseResult<CancelUploadResult> cancelUpload(CancelUploadRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_CANCEL_URL, request, CancelUploadResult.class);
    }

    @Override
    public ResponseResult<ExportResult> loadingExportCacheResult(LoadingExportCacheRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.LOADING_EXPORT_CACHE_RESULT_URL, request, ExportResult.class);
    }

    @Override
    public ResponseResult<DownloadRequestInfo> getRequestInfoByRegisterId(Long registerId) {
        return httpAgent
            .httpPost(RemoteUrlConstants.GET_REQUEST_INFO_RESULT_URL, registerId, DownloadRequestInfo.class);
    }

    @Override
    public ResponseResult<?> resetExecuteProcess(Long registerId) {
        return httpAgent.httpPost(RemoteUrlConstants.RESET_EXECUTE_PROCESS_URL, registerId);
    }

    @Override
    public ResponseResult<?> refreshExecuteProcess(Long registerId, Integer executeProcess,
        UploadStatusEnum nextUploadStatus) {
        RefreshExecuteProcessRequest processRequest = new RefreshExecuteProcessRequest();
        processRequest.setRegisterId(registerId);
        processRequest.setExecuteProcess(executeProcess);
        processRequest.setNextUploadStatus(nextUploadStatus);
        return httpAgent.httpPost(RemoteUrlConstants.REFRESH_EXECUTE_PROCESS_URL, processRequest);
    }

    @Override
    public ResponseResult<List<AppTree>> getAppTree() {
        return httpAgent
            .httpPost(RemoteUrlConstants.GET_APP_TREE_URL, null,
                new TypeReference<ResponseResult<List<AppTree>>>() {
                });
    }
}
