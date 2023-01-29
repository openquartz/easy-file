package org.svnee.easyfile.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.bean.Pagination;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.CancelUploadRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.EnableRunningRequest;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.ListDownloadResultRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RefreshExecuteProcessRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.CancelUploadResult;
import org.svnee.easyfile.common.response.DownloadResult;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.common.util.TypeReference;
import org.svnee.easyfile.storage.EasyFileClient;
import org.svnee.easyfile.storage.remote.HttpAgent;

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
    public ResponseResult<String> download(DownloadRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_FILE_URL, request, String.class);
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
    public ResponseResult<?> refreshExecuteProcess(Long registerId, Integer executeProcess) {
        RefreshExecuteProcessRequest processRequest = new RefreshExecuteProcessRequest();
        processRequest.setRegisterId(registerId);
        processRequest.setExecuteProcess(executeProcess);
        return httpAgent.httpPost(RemoteUrlConstants.REFRESH_EXECUTE_PROCESS_URL, processRequest);
    }
}
