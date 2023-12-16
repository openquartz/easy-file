package com.openquartz.easyfile.core.executor.process;

import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.HandleStatusEnum;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;

/**
 * 执行进度上报器
 *
 * @author svnee
 **/
@Slf4j
public class ExecuteProcessReporterImpl implements ExecuteProcessReporter {

    private final Long registerId;
    private final FileTaskStorageService fileTaskStorageService;

    public ExecuteProcessReporterImpl(Long registerId,
        FileTaskStorageService fileTaskStorageService) {
        this.registerId = registerId;
        this.fileTaskStorageService = fileTaskStorageService;
    }

    @Override
    public void start() {
        try {
            fileTaskStorageService.resetExecuteProcess(registerId);
        } catch (Exception ex) {
            log.error("[ExecuteProcessReporterImpl#start] start-error!,registerId:{}", registerId, ex);
        }
    }

    @Override
    public void report(Integer executeProcess) {
        if (log.isDebugEnabled()) {
            log.debug("[ExecuteProcessReporterImpl#report] registerId:{},executeProcess:{}", registerId,
                executeProcess);
        }
        try {
            HandleStatusEnum nextStatus = HandleStatusEnum.EXECUTING;
            if (executeProcess.compareTo(Constants.FULL_PROCESS) == 0) {
                nextStatus = HandleStatusEnum.UPLOADING;
            }
            fileTaskStorageService.refreshExecuteProgress(registerId, executeProcess, nextStatus);
        } catch (Exception ex) {
            log.error("[ExecuteProcessReporterImpl#report] report-error!,registerId:{},executeProcess:{}", registerId,
                executeProcess, ex);
        }
    }

    @Override
    public void complete() {
        try {
            fileTaskStorageService
                .refreshExecuteProgress(registerId, Constants.FULL_PROCESS, HandleStatusEnum.UPLOADING);
        } catch (Exception ex) {
            log.error("[ExecuteProcessReporterImpl#complete] complete-error!,registerId:{}", registerId, ex);
        }
    }
}
