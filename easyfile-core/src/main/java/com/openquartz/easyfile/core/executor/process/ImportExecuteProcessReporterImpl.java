package com.openquartz.easyfile.core.executor.process;

import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.storage.imports.ImportStorageService;

/**
 * 导入执行进度上报器默认实现
 *
 * @author svnee
 */
@Slf4j
public class ImportExecuteProcessReporterImpl implements ImportExecuteProcessReporter {

    private final Long registerId;
    private final ImportStorageService importStorageService;

    public ImportExecuteProcessReporterImpl(Long registerId, ImportStorageService importStorageService) {
        this.registerId = registerId;
        this.importStorageService = importStorageService;
    }

    @Override
    public void start() {
        try {
            importStorageService.markExecuting(registerId);
        } catch (Exception ex) {
            log.error("[ImportExecuteProcessReporterImpl#start] start-error!,registerId:{}", registerId, ex);
        }
    }

    @Override
    public void report(Integer executeProcess, Long resumePosition) {
        try {
            importStorageService.refreshProgress(registerId, executeProcess, resumePosition);
        } catch (Exception ex) {
            log.error(
                "[ImportExecuteProcessReporterImpl#report] report-error!,registerId:{},executeProcess:{},resumePosition:{}",
                registerId, executeProcess, resumePosition, ex);
        }
    }
}
