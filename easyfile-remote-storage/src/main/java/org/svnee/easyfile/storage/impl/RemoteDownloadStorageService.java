package org.svnee.easyfile.storage.impl;

import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * 调用远程服务的下载存储服务
 *
 * @author svnee
 **/
public class RemoteDownloadStorageService implements DownloadStorageService {

    @Override
    public boolean enableRunning(Long registerId) {
        // TODO: 2022/3/18 待处理
        return false;
    }

    @Override
    public void uploadCallback(UploadCallbackRequest request) {

    }

    @Override
    public Long register(RegisterDownloadRequest downloadRequest) {
        return null;
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {

    }
}
