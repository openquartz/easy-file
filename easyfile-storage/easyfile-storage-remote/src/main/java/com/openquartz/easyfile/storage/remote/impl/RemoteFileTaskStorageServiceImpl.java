package com.openquartz.easyfile.storage.remote.impl;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;
import com.openquartz.easyfile.storage.remote.exception.RemoteUploadExceptionCode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelUploadRequest;
import com.openquartz.easyfile.common.request.DownloadRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.common.request.ListDownloadResultRequest;
import com.openquartz.easyfile.common.request.LoadingExportCacheRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.request.UploadCallbackRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelUploadResult;
import com.openquartz.easyfile.common.response.DownloadResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ExportResult;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.storage.remote.EasyFileClient;

/**
 * 调用远程服务的下载存储服务
 *
 * @author svnee
 **/
@Slf4j
public class RemoteFileTaskStorageServiceImpl implements FileTaskStorageService {

    private final EasyFileClient easyFileClient;

    public RemoteFileTaskStorageServiceImpl(EasyFileClient easyFileClient) {
        this.easyFileClient = easyFileClient;
    }

    @Override
    public boolean enableRunning(Long registerId) {
        log.info("[RemoteDownloadStorageServiceImpl#enableRunning] request registerId:{}", registerId);
        ResponseResult<Boolean> result = easyFileClient
                .enableRunning(EnableRunningRequest.create(registerId));
        log.info("[RemoteDownloadStorageServiceImpl#enableRunning] response registerId:{},result:{}", registerId,
                result);
        if (Objects.nonNull(result) && result.isSuccess()) {
            return result.getData();
        }
        return false;
    }

    @Override
    public ExportResult loadingCacheExportResult(LoadingExportCacheRequest request) {
        log.info("[RemoteDownloadStorageServiceImpl#loadingCacheExportResult] request:{}", request);
        try {
            ResponseResult<ExportResult> result = easyFileClient.loadingExportCacheResult(request);
            log.info("[RemoteDownloadStorageServiceImpl#loadingCacheExportResult] request:{},response:{}", request,
                    result);
            if (Objects.nonNull(result)) {
                return result.getData();
            }
        } catch (Exception ex) {
            log.error("[RemoteDownloadStorageServiceImpl#loadingCacheExportResult] request:{}", request, ex);
        }
        return null;
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {
        log.info("[RemoteDownloadStorageServiceImpl#uploadCallback] upload param:{}", request);
        Retryer<ResponseResult<?>> retryer = RetryerBuilder.<ResponseResult<?>>newBuilder()
                .retryIfExceptionOfType(IOException.class)
                .withWaitStrategy(WaitStrategies.incrementingWait(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            log.error("[RemoteDownloadStorageServiceImpl#uploadCallback] retry service error",
                                    attempt.getExceptionCause());
                        }
                    }
                })
                .build();
        try {
            ResponseResult<?> callbackResponse = retryer.call(() -> easyFileClient.uploadCallback(request));
            log.info("[RemoteDownloadStorageServiceImpl#uploadCallback] upload param:{},response:{}", request,
                    JSONUtil.toJson(request));
            Asserts.notNull(callbackResponse, RemoteUploadExceptionCode.UPLOAD_CALLBACK_ERROR);
            Asserts.isTrue(callbackResponse.isSuccess(), RemoteUploadExceptionCode.UPLOAD_CALLBACK_ERROR);
        } catch (Exception ex) {
            log.error("[RemoteDownloadStorageServiceImpl#uploadCallback] call easyfile-client error,request:{}",
                    request,
                    ex);
            throw new EasyFileException(RemoteUploadExceptionCode.UPLOAD_CALLBACK_ERROR);
        }
    }

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        ResponseResult<Long> registerResult = easyFileClient.register(downloadRequest);
        if (Objects.nonNull(registerResult) && registerResult.isSuccess()) {
            return registerResult.getData();
        }
        throw new EasyFileException(RemoteUploadExceptionCode.REGISTER_DOWNLOAD_ERROR);
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        easyFileClient.autoRegisterTask(request);
    }

    @Override
    public DownloadUrlResult download(DownloadRequest request) {
        ResponseResult<DownloadUrlResult> responseResult = easyFileClient.download(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        return responseResult.getData();
    }

    @Override
    public CancelUploadResult cancelUpload(CancelUploadRequest request) {
        ResponseResult<CancelUploadResult> responseResult = easyFileClient.cancelUpload(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
                responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public DownloadRequestInfo getRequestInfoByRegisterId(Long registerId) {
        ResponseResult<DownloadRequestInfo> responseResult = easyFileClient.getRequestInfoByRegisterId(registerId);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
                responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, HandleStatusEnum nextUploadStatus) {
        if (executeProcess <= 0) {
            return;
        }
        easyFileClient.refreshExecuteProcess(registerId, executeProcess, nextUploadStatus);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        easyFileClient.resetExecuteProcess(registerId);
    }

    @Override
    public Pagination<DownloadResult> listExportResult(ListDownloadResultRequest request) {
        ResponseResult<Pagination<DownloadResult>> responseResult = easyFileClient.listExportResult(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
                responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public List<AppTree> getAppTree() {
        ResponseResult<List<AppTree>> responseResult = easyFileClient.getAppTree();
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
                responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public Locale getCurrentLocale(Long registerId) {

        ResponseResult<String> responseResult = easyFileClient.getCurrentLocale(registerId);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
                responseResult.getMessage());
        String data = responseResult.getData();
        if (StringUtils.isNotBlank(data)) {
            return Locale.forLanguageTag(data);
        }
        return null;
    }
}
