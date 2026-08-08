package com.openquartz.easyfile.storage.remote.impl;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.exception.ExpandExecutorErrorCode;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import com.openquartz.easyfile.storage.remote.exception.RemoteUploadExceptionCode;

/**
 * 远程服务端统一限流
 *
 * @author svnee
 **/
public class RemoteLimitingServiceImpl implements LimitingService {

    public static final int DEFAULT_MAX_CONCURRENT_IMPORT_TASKS = 10;

    private final EasyFileClient easyFileClient;
    private final int maxConcurrentImportTasks;

    public RemoteLimitingServiceImpl(EasyFileClient easyFileClient) {
        this(easyFileClient, DEFAULT_MAX_CONCURRENT_IMPORT_TASKS);
    }

    public RemoteLimitingServiceImpl(EasyFileClient easyFileClient, int maxConcurrentImportTasks) {
        this.easyFileClient = easyFileClient;
        this.maxConcurrentImportTasks = maxConcurrentImportTasks > 0
            ? maxConcurrentImportTasks : DEFAULT_MAX_CONCURRENT_IMPORT_TASKS;
    }

    @Override
    public void limiting(ExportLimitingRequest request) {
        ResponseResult<?> responseResult = easyFileClient.limiting(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
    }

    /**
     * Import limiting via remote list counts (NONE + EXECUTING), mirroring local record-count gate.
     */
    @Override
    public void limiting(ImportLimitingRequest request) {
        if (request == null || StringUtils.isBlank(request.getAppId())) {
            return;
        }
        int inFlight = 0;
        inFlight += countRemote(request, ImportStatusEnum.NONE);
        inFlight += countRemote(request, ImportStatusEnum.EXECUTING);
        if (inFlight >= maxConcurrentImportTasks) {
            throw EasyFileException.replacePlaceHold(
                ExpandExecutorErrorCode.IMPORT_LIMITING_EXCEEDED_ERROR,
                request.getAppId(), request.getImportCode());
        }
    }

    private int countRemote(ImportLimitingRequest request, ImportStatusEnum status) {
        ImportResultQueryRequest query = ImportResultQueryRequest.builder()
            .appId(request.getAppId())
            .importCode(request.getImportCode())
            .importStatus(status.getCode())
            .build();
        query.setPageNum(1);
        query.setPageSize(1);
        ResponseResult<Pagination<ImportResult>> responseResult = easyFileClient.listImportResult(query);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        Pagination<ImportResult> page = responseResult.getData();
        if (page == null || page.getTotalRecords() == null) {
            return 0;
        }
        return page.getTotalRecords().intValue();
    }
}
