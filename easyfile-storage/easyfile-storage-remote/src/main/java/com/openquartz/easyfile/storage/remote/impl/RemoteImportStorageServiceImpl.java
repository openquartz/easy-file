package com.openquartz.easyfile.storage.remote.impl;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.EnableRunningRequest;
import com.openquartz.easyfile.common.request.ImportCompleteRequest;
import com.openquartz.easyfile.common.request.ImportOperatorRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.request.ImportUpdateRequest;
import com.openquartz.easyfile.common.request.RefreshImportExecuteProcessRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import com.openquartz.easyfile.storage.remote.exception.RemoteUploadExceptionCode;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 调用远程服务的导入存储服务
 *
 * @author svnee
 */
@Slf4j
public class RemoteImportStorageServiceImpl implements ImportStorageService {

    private final EasyFileClient easyFileClient;

    public RemoteImportStorageServiceImpl(EasyFileClient easyFileClient) {
        this.easyFileClient = easyFileClient;
    }

    @Override
    public boolean enableRunning(Long registerId) {
        log.info("[RemoteImportStorageServiceImpl#enableRunning] request registerId:{}", registerId);
        ResponseResult<Boolean> result = easyFileClient.enableImportRunning(EnableRunningRequest.create(registerId));
        log.info("[RemoteImportStorageServiceImpl#enableRunning] response registerId:{},result:{}", registerId,
            result);
        if (Objects.nonNull(result) && result.isSuccess()) {
            return Boolean.TRUE.equals(result.getData());
        }
        return false;
    }

    @Override
    public Long register(ImportRegisterRequest request) {
        ResponseResult<Long> registerResult = easyFileClient.registerImport(request);
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
    public ImportRequestInfo getRequestInfoByRegisterId(Long registerId) {
        ResponseResult<ImportRequestInfo> responseResult = easyFileClient.getImportRequestInfoByRegisterId(registerId);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                       ImportStatusEnum nextStatus) {
        if (executeProcess == null || executeProcess <= 0) {
            return;
        }
        RefreshImportExecuteProcessRequest processRequest = new RefreshImportExecuteProcessRequest();
        processRequest.setRegisterId(registerId);
        processRequest.setExecuteProcess(executeProcess);
        processRequest.setResumePosition(resumePosition);
        processRequest.setNextStatus(nextStatus);
        easyFileClient.refreshImportExecuteProcess(processRequest);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        easyFileClient.resetImportExecuteProcess(registerId);
    }

    @Override
    public void updateTotalCount(Long registerId, Long totalCount) {
        ImportUpdateRequest request = new ImportUpdateRequest();
        request.setRegisterId(registerId);
        request.setTotalCount(totalCount);
        easyFileClient.updateImportTotalCount(request);
    }

    @Override
    public void updateFileHash(Long registerId, String fileHash) {
        ImportUpdateRequest request = new ImportUpdateRequest();
        request.setRegisterId(registerId);
        request.setFileHash(fileHash);
        easyFileClient.updateImportFileHash(request);
    }

    @Override
    public void updateResultFileUrl(Long registerId, String resultFileUrl) {
        ImportUpdateRequest request = new ImportUpdateRequest();
        request.setRegisterId(registerId);
        request.setResultFileUrl(resultFileUrl);
        easyFileClient.updateImportResultFileUrl(request);
    }

    @Override
    public void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                         Long failCount, Long resumePosition, String errorMsg) {
        ImportCompleteRequest request = new ImportCompleteRequest();
        request.setRegisterId(registerId);
        request.setStatus(status);
        request.setTotalCount(totalCount);
        request.setSuccessCount(successCount);
        request.setFailCount(failCount);
        request.setResumePosition(resumePosition);
        request.setErrorMsg(errorMsg);
        easyFileClient.completeImport(request);
    }

    @Override
    public CancelImportResult cancelImport(CancelImportRequest request) {
        ResponseResult<CancelImportResult> responseResult = easyFileClient.cancelImport(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        ResponseResult<Pagination<ImportResult>> responseResult = easyFileClient.listImportResult(request);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public boolean retry(Long registerId, String operator) {
        ImportOperatorRequest request = new ImportOperatorRequest();
        request.setRegisterId(registerId);
        request.setOperator(operator);
        ResponseResult<Boolean> responseResult = easyFileClient.retryImport(request);
        if (Objects.nonNull(responseResult) && responseResult.isSuccess()) {
            return Boolean.TRUE.equals(responseResult.getData());
        }
        return false;
    }

    @Override
    public boolean revoke(Long registerId, String operator) {
        ImportOperatorRequest request = new ImportOperatorRequest();
        request.setRegisterId(registerId);
        request.setOperator(operator);
        ResponseResult<Boolean> responseResult = easyFileClient.revokeImport(request);
        if (Objects.nonNull(responseResult) && responseResult.isSuccess()) {
            return Boolean.TRUE.equals(responseResult.getData());
        }
        return false;
    }

    @Override
    public DownloadUrlResult resultFile(Long registerId) {
        ResponseResult<DownloadUrlResult> responseResult = easyFileClient.importResultFile(registerId);
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        return responseResult.getData();
    }

    @Override
    public List<AppTree> getAppTree() {
        ResponseResult<List<AppTree>> responseResult = easyFileClient.getImportAppTree();
        Asserts.notNull(responseResult, RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_ERROR);
        Asserts.isTrue(responseResult.isSuccess(), RemoteUploadExceptionCode.DOWNLOAD_RESPONSE_MSG_ERROR,
            responseResult.getMessage());
        return responseResult.getData();
    }
}
