package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.core.executor.AsyncFileExportHandlerAdapter;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.core.executor.support.FileTriggerContext;
import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * 数据DB
 *
 * @author svnee
 **/
@Slf4j
public abstract class DatabaseAsyncFileExportHandlerAdapter extends AsyncFileExportHandlerAdapter implements InitializingBean {

    private final FileTriggerService triggerService;
    private final DownloadStorageService storageService;
    private final IDatabaseAsyncHandlerProperty handlerProperties;

    private final ScheduledThreadPoolExecutor compensateScheduleExecutorService = new ScheduledThreadPoolExecutor(1,
        new ThreadFactoryBuilder()
            .setNameFormat("CompensateScheduleAsyncHandler-thread-%d")
            .build());

    protected DatabaseAsyncFileExportHandlerAdapter(
        IEasyFileDownloadProperty downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        FileTriggerService triggerService,
        IDatabaseAsyncHandlerProperty handlerProperties) {
        super(downloadProperties, uploadService, storageService);
        this.triggerService = triggerService;
        this.handlerProperties = handlerProperties;
        this.storageService = storageService;
    }

    @Override
    public void execute(BaseExportExecutor executor, BaseExportRequestContext baseRequest, Long registerId) {
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
            try {
                DownloadRequestInfo requestInfo = storageService.getRequestInfoByRegisterId(k.getRegisterId());
                BaseExportExecutor executor = FileExportExecutorSupport
                    .get(requestInfo.getDownloadCode());

                // set async trigger if absent
                FileTriggerContext.setAsyncTriggerFlagIfAbsent(true);
                doExecute(executor, requestInfo.getRequestContext(), k.getRegisterId());
                triggerService.exeSuccess(k.getRegisterId());
            } catch (Exception ex) {
                log.error("[DatabaseAsyncFileHandlerAdapter#doTrigger] execute-failed!registerId:{}", k.getRegisterId(),
                    ex);
                triggerService.exeFail(k.getRegisterId());
            } finally {
                // clear context
                FileTriggerContext.clear();
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
