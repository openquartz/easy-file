package org.svnee.easyfile.storage.impl;

import org.svnee.easyfile.common.request.AutoTaskRegisterRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.request.UploadCallbackRequest;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * 本地下载存储
 *
 * @author svnee
 **/
public class LocalDownloadStorageService implements DownloadStorageService {

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
