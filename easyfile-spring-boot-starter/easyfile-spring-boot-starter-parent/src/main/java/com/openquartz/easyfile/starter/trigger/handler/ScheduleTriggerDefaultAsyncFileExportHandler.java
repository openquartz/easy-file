package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.BaseDefaultRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.impl.DatabaseDefaultAsyncFileExportHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时调度的触发异步文件下载处理器
 *
 * @author svnee
 **/
@Slf4j
public class ScheduleTriggerDefaultAsyncFileExportHandler extends DatabaseDefaultAsyncFileExportHandler implements InitializingBean {

    private final FileTriggerService triggerService;
    private final ScheduledThreadPoolExecutor scheduleExecutorService;
    private final ScheduleAsyncHandlerProperties handlerProperties;
    private final ScheduledThreadPoolExecutor reaperScheduleExecutorService;

    private ScheduledThreadPoolExecutor init(ScheduleAsyncHandlerProperties handlerProperties,
        BaseDefaultRejectExecutionHandler rejectHandler) {
        return new ScheduledThreadPoolExecutor(handlerProperties.getThreadPoolCoreSize(),
            new ThreadFactoryBuilder()
                .setNameFormat(handlerProperties.getThreadPoolThreadPrefix() + "-thread-%d")
                .build(),
            rejectHandler);
    }

    public ScheduleTriggerDefaultAsyncFileExportHandler(
        DownloadStorageService storageService,
        FileTriggerService triggerService,
        ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties,
        AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory,
        BaseDefaultRejectExecutionHandler rejectExecutionHandler) {

        super(storageService,triggerService,scheduleAsyncHandlerProperties,asyncFileTriggerExecuteHandlerFactory);

        this.triggerService = triggerService;
        this.handlerProperties = scheduleAsyncHandlerProperties;
        this.scheduleExecutorService = init(scheduleAsyncHandlerProperties, rejectExecutionHandler);
        // reaper
        this.reaperScheduleExecutorService = new ScheduledThreadPoolExecutor(
            handlerProperties.getReaperTheadPoolCoreSize(),
            new ThreadFactoryBuilder()
                .setNameFormat(handlerProperties.getReaperThreadPoolThreadPrefix() + "-thread-%d")
                .build(),
            rejectExecutionHandler);
    }

    public void doTrigger() {
        List<DownloadTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    public void doReaperTrigger() {
        List<DownloadTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getMinReaperSeconds(), handlerProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    private void doActualTrigger(List<DownloadTriggerEntry> registerIdList) {
        log.info("[ScheduleTriggerAsyncFileHandler#doTrigger] start.....");
        try {

            if (CollectionUtils.isEmpty(registerIdList)) {
                log.info("[ScheduleTriggerAsyncFileHandler#doTrigger] end.....");
                return;
            }
            registerIdList.stream().sorted().forEach(this::doTrigger);
        } catch (Exception ex) {
            log.error("[ScheduleTriggerAsyncFileHandler#doTrigger] error!", ex);
        }
        log.info("[ScheduleTriggerAsyncFileHandler#doTrigger] end.....");
    }


    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        double initDelaySeconds = new Random(1).nextDouble() * handlerProperties.getSchedulePeriod();
        scheduleExecutorService
            .scheduleAtFixedRate(this::doTrigger, (int) initDelaySeconds, handlerProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
        // do reaper
        reaperScheduleExecutorService
            .scheduleAtFixedRate(this::doReaperTrigger, (int) initDelaySeconds, handlerProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
    }

}
