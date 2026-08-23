package com.openquartz.easyfile.starter.trigger.retry;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadRetryScanner {

    private final DownloadStorageService downloadStorageService;
    private final BaseAsyncFileHandler handler;
    private final EasyFileDownloadRetryProperties properties;
    private final EasyFileDownloadProperties downloadProperties;

    public DownloadRetryScanner(DownloadStorageService downloadStorageService, BaseAsyncFileHandler handler,
        EasyFileDownloadRetryProperties properties, EasyFileDownloadProperties downloadProperties) {
        this.downloadStorageService = downloadStorageService;
        this.handler = handler;
        this.properties = properties;
        this.downloadProperties = downloadProperties;
    }

    public void scan() {
        try {
            String appId = downloadProperties.getAppId();
            if (StringUtils.isBlank(appId)) {
                log.warn("[DownloadRetryScanner#scan] skip, blank appId");
                return;
            }
            List<Long> ids = downloadStorageService.listRetryable(properties.getLookBackHours(),
                properties.getScanBatchSize(), properties.getOrphanGraceSeconds(), appId);
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
        } catch (Exception ex) {
            log.error("[DownloadRetryScanner#scan] listRetryable failed", ex);
        }
    }
}
