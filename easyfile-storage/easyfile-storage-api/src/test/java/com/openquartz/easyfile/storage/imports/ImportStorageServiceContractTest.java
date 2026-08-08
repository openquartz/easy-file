package com.openquartz.easyfile.storage.imports;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import org.junit.Test;

public class ImportStorageServiceContractTest {

    @Test
    public void registerAndEnableRunning_arePartOfContract() {
        ImportStorageService svc = new ImportStorageService() {
            @Override public boolean enableRunning(Long registerId) { return true; }
            @Override public Long register(ImportRegisterRequest request) { return 1L; }
            @Override public com.openquartz.easyfile.common.bean.ImportRequestInfo getRequestInfoByRegisterId(Long id) { return null; }
            @Override public void refreshExecuteProgress(Long id, Integer p, Long resume, ImportStatusEnum s) {}
            @Override public void resetExecuteProcess(Long registerId) {}
            @Override public void updateTotalCount(Long registerId, Long totalCount) {}
            @Override public void updateFileHash(Long registerId, String fileHash) {}
            @Override public void updateResultFileUrl(Long registerId, String resultFileUrl) {}
            @Override public void complete(Long id, ImportStatusEnum status, Long total, Long success, Long fail, Long resume, String err) {}
            @Override public com.openquartz.easyfile.common.response.CancelImportResult cancelImport(
                    com.openquartz.easyfile.common.request.CancelImportRequest request) { return null; }
            // keep existing list/retry/revoke/resultFile/getAppTree defaults or override
            @Override public void markExecuting(Long registerId) { enableRunning(registerId); }
            @Override public void refreshProgress(Long registerId, Integer process, Long resumePosition) {
                refreshExecuteProgress(registerId, process, resumePosition, ImportStatusEnum.EXECUTING);
            }
        };
        org.junit.Assert.assertTrue(svc.enableRunning(1L));
        org.junit.Assert.assertEquals(Long.valueOf(1L), svc.register(ImportRegisterRequest.builder().importCode("x").build()));
    }
}
