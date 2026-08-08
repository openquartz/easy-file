package com.openquartz.easyfile.storage.remote.impl;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import com.openquartz.easyfile.storage.remote.exception.RemoteUploadExceptionCode;

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
        ResponseResult<?> responseResult = easyFileClient.limiting(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
    }
}
