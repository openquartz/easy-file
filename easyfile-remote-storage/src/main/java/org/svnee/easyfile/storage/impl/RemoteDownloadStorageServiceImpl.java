package org.svnee.easyfile.storage.impl;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.EnableRunningRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.storage.EasyFileClient;
import org.svnee.easyfile.storage.download.DownloadStorageService;

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
        ResponseResult<Boolean> result = easyFileClient
            .enableRunning(EnableRunningRequest.create(registerId));
        if (Objects.nonNull(result) && result.isSuccess()) {
            return result.getData();
        }
        return false;
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {
        easyFileClient.uploadCallback(request)
    }

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        ResponseResult<Long> registerResult = easyFileClient.register(downloadRequest);
        if (Objects.nonNull(registerResult) && registerResult.isSuccess()) {
            return registerResult.getData();
        }
        return null;
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        easyFileClient.autoRegisterTask(request);
    }
}
