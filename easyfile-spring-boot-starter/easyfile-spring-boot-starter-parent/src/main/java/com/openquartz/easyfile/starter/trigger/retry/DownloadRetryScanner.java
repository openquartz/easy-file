package com.openquartz.easyfile.starter.trigger.retry;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadRetryScanner {

    private final DownloadStorageService downloadStorageService;
    private final BaseAsyncFileHandler handler;
    private final EasyFileDownloadRetryProperties properties;

    public DownloadRetryScanner(DownloadStorageService downloadStorageService, BaseAsyncFileHandler handler,
        EasyFileDownloadRetryProperties properties) {
        this.downloadStorageService = downloadStorageService;
        this.handler = handler;
        this.properties = properties;
    }

    public void scan() {
        List<Long> ids = downloadStorageService.listRetryable(properties.getLookBackHours(),
            properties.getScanBatchSize(), properties.getOrphanGraceSeconds());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (Long registerId : ids) {
            try {
                downloadStorageService.claimRetry(registerId, Constants.SYSTEM_USER);
                handler.reexecute(registerId);
            } catch (Exception ex) {
                log.error("[DownloadRetryScanner#scan] registerId:{}", registerId, ex);
            }
        }
    }
}
