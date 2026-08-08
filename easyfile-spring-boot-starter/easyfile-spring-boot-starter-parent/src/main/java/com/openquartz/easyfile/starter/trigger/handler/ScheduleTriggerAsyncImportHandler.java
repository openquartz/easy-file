package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.impl.DatabaseAsyncImportHandlerAdapter;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.imports.ImportTriggerService;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * 定时调度的触发异步导入处理器
 *
 * @author svnee
 */
@Slf4j
public class ScheduleTriggerAsyncImportHandler extends DatabaseAsyncImportHandlerAdapter implements InitializingBean {

    private static final String TRIGGER_THREAD_PREFIX = "ScheduleAsyncImportHandler";
    private static final String REAPER_THREAD_PREFIX = "ReaperScheduleAsyncImportHandler";
    private static final int TRIGGER_THREAD_POOL_CORE_SIZE = 2;
    private static final int REAPER_THREAD_POOL_CORE_SIZE = 1;

    private final ImportTriggerService triggerService;
    private final ScheduledThreadPoolExecutor scheduleExecutorService;
    private final EasyFileImportProperties importProperties;
    private final ScheduledThreadPoolExecutor reaperScheduleExecutorService;

    public ScheduleTriggerAsyncImportHandler(
        EasyFileImportProperties importProperties,
        ImportStorageService storageService,
        ImportTriggerService triggerService,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler) {
        this(importProperties, storageService, triggerService, null, rejectExecutionHandler);
    }

    public ScheduleTriggerAsyncImportHandler(
        EasyFileImportProperties importProperties,
        ImportStorageService storageService,
        ImportTriggerService triggerService,
        IdempotentImportManager idempotentImportManager,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler) {
        super(storageService, triggerService, importProperties, idempotentImportManager);
        this.triggerService = triggerService;
        this.importProperties = importProperties;
        this.scheduleExecutorService = new ScheduledThreadPoolExecutor(
            TRIGGER_THREAD_POOL_CORE_SIZE,
            new ThreadFactoryBuilder()
                .setNameFormat(TRIGGER_THREAD_PREFIX + "-thread-%d")
                .build(),
            rejectExecutionHandler);
        this.reaperScheduleExecutorService = new ScheduledThreadPoolExecutor(
            REAPER_THREAD_POOL_CORE_SIZE,
            new ThreadFactoryBuilder()
                .setNameFormat(REAPER_THREAD_PREFIX + "-thread-%d")
                .build(),
            rejectExecutionHandler);
    }

    public void doTrigger() {
        List<ImportTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(importProperties.getLookBackHours(), importProperties.getMaxTriggerCount(),
                importProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    public void doReaperTrigger() {
        List<ImportTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(importProperties.getLookBackHours(), importProperties.getMaxTriggerCount(),
                importProperties.getMinReaperSeconds(), importProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    private void doActualTrigger(List<ImportTriggerEntry> registerIdList) {
        log.info("[ScheduleTriggerAsyncImportHandler#doTrigger] start.....");
        try {
            if (CollectionUtils.isEmpty(registerIdList)) {
                log.info("[ScheduleTriggerAsyncImportHandler#doTrigger] end.....");
                return;
            }
            registerIdList
                .stream()
                .sorted(Comparator.comparing(ImportTriggerEntry::getRegisterId))
                .forEach(this::doTrigger);
        } catch (Exception ex) {
            log.error("[ScheduleTriggerAsyncImportHandler#doTrigger] error!", ex);
        }
        log.info("[ScheduleTriggerAsyncImportHandler#doTrigger] end.....");
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        double initDelaySeconds = new Random(1).nextDouble() * importProperties.getSchedulePeriod();
        scheduleExecutorService
            .scheduleAtFixedRate(this::doTrigger, (int) initDelaySeconds, importProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
        reaperScheduleExecutorService
            .scheduleAtFixedRate(this::doReaperTrigger, (int) initDelaySeconds, importProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
    }
}
