package org.svnee.easyfile.core.executor.process;

import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.storage.download.DownloadStorageService;

/**
 * 执行进度上报器
 *
 * @author svnee
 **/
@Slf4j
public class ExecuteProcessReporterImpl implements ExecuteProcessReporter {

    private final Long registerId;
    private final DownloadStorageService downloadStorageService;

    public ExecuteProcessReporterImpl(Long registerId,
        DownloadStorageService downloadStorageService) {
        this.registerId = registerId;
        this.downloadStorageService = downloadStorageService;
    }

    @Override
    public void start() {
        try {
            downloadStorageService.resetExecuteProcess(registerId);
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
            UploadStatusEnum nextStatus = UploadStatusEnum.EXECUTING;
            if (executeProcess.compareTo(Constants.FULL_PROCESS) == 0) {
                nextStatus = UploadStatusEnum.UPLOADING;
            }
            downloadStorageService.refreshExecuteProgress(registerId, executeProcess, nextStatus);
        } catch (Exception ex) {
            log.error("[ExecuteProcessReporterImpl#report] report-error!,registerId:{},executeProcess:{}", registerId,
                executeProcess, ex);
        }
    }

    @Override
    public void complete() {
        try {
            downloadStorageService
                .refreshExecuteProgress(registerId, Constants.FULL_PROCESS, UploadStatusEnum.UPLOADING);
        } catch (Exception ex) {
            log.error("[ExecuteProcessReporterImpl#complete] complete-error!,registerId:{}", registerId, ex);
        }
    }
}
