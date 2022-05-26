package org.svnee.easyfile.storage.impl;

import static org.svnee.easyfile.storage.exception.RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.DownloadRequest;
import org.svnee.easyfile.common.request.EnableRunningRequest;
import org.svnee.easyfile.common.request.LoadingExportCacheRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.common.util.JSONUtil;
import org.svnee.easyfile.storage.EasyFileClient;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.exception.RemoteUploadExceptionCode;

/**
 * 调用远程服务的下载存储服务
 *
 * @author svnee
 **/
@Slf4j
public class RemoteDownloadStorageServiceImpl implements DownloadStorageService {

    private final EasyFileClient easyFileClient;

    public RemoteDownloadStorageServiceImpl(EasyFileClient easyFileClient) {
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
            log.warn("[RemoteDownloadStorageServiceImpl#loadingCacheExportResult] request:{},ex:{}", request, ex);
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
    public String download(DownloadRequest request) {
        ResponseResult<String> responseResult = easyFileClient.download(request);
        Asserts.notNull(responseResult, DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), DOWNLOAD_RESPONSE_ERROR);
        return responseResult.getData();
    }
}
