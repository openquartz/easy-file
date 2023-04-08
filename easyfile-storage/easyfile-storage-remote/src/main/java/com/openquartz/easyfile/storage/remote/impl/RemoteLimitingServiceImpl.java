package com.openquartz.easyfile.storage.remote.impl;

import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.remote.EasyFileClient;

/**
 * 远程服务端统一限流
 *
 * @author svnee
 **/
public class RemoteLimitingServiceImpl implements LimitingService {

    private final EasyFileClient easyFileClient;

    public RemoteLimitingServiceImpl(EasyFileClient easyFileClient) {
        this.easyFileClient = easyFileClient;
    }

    @Override
    public void limiting(ExportLimitingRequest request) {
        easyFileClient.limiting(request);
    }
}
