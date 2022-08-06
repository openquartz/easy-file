package org.svnee.easyfile.starter.executor.impl;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.request.DownloadTriggerRequest;
import org.svnee.easyfile.common.response.DownloadTriggerEntry;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.processor.FileExportExecutorSupport;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.DatabaseAsyncHandlerProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * 数据DB
 *
 * @author svnee
 **/
@Slf4j
public abstract class DatabaseAsyncFileHandlerAdapter extends AsyncFileHandlerAdapter implements InitializingBean {

    private final DownloadTriggerService triggerService;
    private final DownloadStorageService storageService;
    private final DatabaseAsyncHandlerProperties handlerProperties;

    private final ScheduledThreadPoolExecutor compensateScheduleExecutorService = new ScheduledThreadPoolExecutor(1,
        new ThreadFactoryBuilder()
            .setNameFormat("CompensateScheduleAsyncHandler-thread-%d")
            .build());

    protected DatabaseAsyncFileHandlerAdapter(
        EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        DownloadTriggerService triggerService,
        DatabaseAsyncHandlerProperties handlerProperties) {
        super(downloadProperties, uploadService, storageService);
        this.triggerService = triggerService;
        this.handlerProperties = handlerProperties;
        this.storageService = storageService;
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        DownloadTriggerRequest triggerRequest = new DownloadTriggerRequest();
        triggerRequest.setRegisterId(registerId);
        triggerService.trigger(triggerRequest);
    }

    /**
     * 做补偿
     */
    public void doCompensate() {
        log.info("[DatabaseAsyncFileHandlerAdapter#doCompensate] start...");
        try {
            triggerService.handleExpirationTrigger(handlerProperties.getMaxExecuteTimeout());
            triggerService
                .archiveHistoryTrigger(handlerProperties.getMaxArchiveHours(), handlerProperties.getMaxTriggerCount());
        } catch (Exception ex) {
            log.error("[DatabaseAsyncFileHandlerAdapter#doCompensate] error!...", ex);
        }
        log.info("[DatabaseAsyncFileHandlerAdapter#doCompensate] end...");
    }

    public void doTrigger(DownloadTriggerEntry k) {
        boolean execute = triggerService.startExecute(k.getRegisterId(), k.getTriggerCount());
        if (execute) {
            DownloadRequestInfo requestInfo = storageService.getRequestInfoByRegisterId(k.getRegisterId());
            try {
                BaseDownloadExecutor executor = FileExportExecutorSupport
                    .get(requestInfo.getDownloadCode());
                doExecute((BaseDownloadExecutor) SpringContextUtil.getTarget(executor),
                    requestInfo.getRequestContext(), k.getRegisterId());
                triggerService.exeSuccess(k.getRegisterId());
            } catch (Exception ex) {
                triggerService.exeFail(k.getRegisterId());
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        compensateScheduleExecutorService
            .scheduleAtFixedRate(this::doCompensate, handlerProperties.getSchedulePeriod(),
                handlerProperties.getMaxExecuteTimeout(), TimeUnit.SECONDS);
    }
}
