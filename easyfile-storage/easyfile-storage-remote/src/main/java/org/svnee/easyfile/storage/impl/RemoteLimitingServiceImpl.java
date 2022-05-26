package org.svnee.easyfile.storage.impl;

import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.storage.EasyFileClient;
import org.svnee.easyfile.storage.download.LimitingService;

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
