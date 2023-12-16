package com.openquartz.easyfile.core.executor.impl;

import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.bean.IRequest;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandler;
import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.Executor;
import com.openquartz.easyfile.core.executor.FileHandlerExecutor;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.core.executor.support.FileTriggerContext;
import com.openquartz.easyfile.core.property.IDatabaseAsyncHandlerProperty;
import com.openquartz.easyfile.storage.download.FileTaskStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据DB
 *
 * @author svnee
 **/
@Slf4j
public abstract class DatabaseDefaultAsyncFileExportHandler implements AsyncFileTriggerExecuteHandler, InitializingBean {

    private final FileTriggerService triggerService;
    private final FileTaskStorageService storageService;
    private final IDatabaseAsyncHandlerProperty handlerProperties;
    private final AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory;

    private final ScheduledThreadPoolExecutor compensateScheduleExecutorService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder()
                    .setNameFormat("CompensateScheduleAsyncHandler-thread-%d")
                    .build());

    protected DatabaseDefaultAsyncFileExportHandler(
            FileTaskStorageService storageService,
            FileTriggerService triggerService,
            IDatabaseAsyncHandlerProperty handlerProperties,
            AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory) {
        this.triggerService = triggerService;
        this.handlerProperties = handlerProperties;
        this.storageService = storageService;
        this.asyncFileTriggerExecuteHandlerFactory = asyncFileTriggerExecuteHandlerFactory;
    }

    @Override
    public <T extends Executor, R extends IRequest> void execute(T executor, R baseRequest, Long registerId) {
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

    @SuppressWarnings("all")
    public void doTrigger(DownloadTriggerEntry k) {
        if (triggerService.startExecute(k.getRegisterId(), k.getTriggerCount())) {
            try {
                DownloadRequestInfo requestInfo = storageService.getRequestInfoByRegisterId(k.getRegisterId());

                Executor executor = FileExportExecutorSupport.get(requestInfo.getDownloadCode());

                // set async trigger if absent
                FileTriggerContext.setAsyncTriggerFlagIfAbsent(true);

                FileHandlerExecutor handlerExecutor = asyncFileTriggerExecuteHandlerFactory.get(executor.getClass(), requestInfo.getRequestContext().getClass());
                handlerExecutor.execute(executor, requestInfo.getRequestContext(), k.getRegisterId());

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
