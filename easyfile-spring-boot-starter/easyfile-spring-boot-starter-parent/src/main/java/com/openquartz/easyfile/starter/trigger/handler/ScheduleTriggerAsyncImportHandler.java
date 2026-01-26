package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.bean.ImportRecordInfo;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.core.executor.AsyncImportHandler;
import com.openquartz.easyfile.core.executor.BaseAsyncImportExecutor;
import com.openquartz.easyfile.core.executor.support.FileImportExecutorSupport;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncImportHandlerProperties;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import com.openquartz.easyfile.storage.importer.ImportTriggerService;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * Schedule Trigger Async Import Handler
 *
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class ScheduleTriggerAsyncImportHandler implements InitializingBean {

    private final ImportTriggerService triggerService;
    private final ImportStorageService storageService;
    private final AsyncImportHandler asyncImportHandler;
    private final ScheduleAsyncImportHandlerProperties handlerProperties;

    private ScheduledThreadPoolExecutor scheduleExecutorService;
    private ScheduledThreadPoolExecutor reaperScheduleExecutorService;
    private ScheduledThreadPoolExecutor compensateScheduleExecutorService;

    private ScheduledThreadPoolExecutor initExecutor(int corePoolSize, String threadPrefix) {
        return new ScheduledThreadPoolExecutor(corePoolSize,
            new ThreadFactoryBuilder()
                .setNameFormat(threadPrefix + "-thread-%d")
                .build(),
            new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public void afterPropertiesSet() {
        this.scheduleExecutorService = initExecutor(handlerProperties.getThreadPoolCoreSize(),
            handlerProperties.getThreadPoolThreadPrefix());
        this.reaperScheduleExecutorService = initExecutor(handlerProperties.getReaperTheadPoolCoreSize(),
            handlerProperties.getReaperThreadPoolThreadPrefix());
        this.compensateScheduleExecutorService = initExecutor(1, "CompensateScheduleAsyncImportHandler");

        double initDelaySeconds = new Random(1).nextDouble() * handlerProperties.getSchedulePeriod();
        scheduleExecutorService
            .scheduleAtFixedRate(this::doTrigger, (int) initDelaySeconds, handlerProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
        
        reaperScheduleExecutorService
            .scheduleAtFixedRate(this::doReaperTrigger, (int) initDelaySeconds, handlerProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);

        compensateScheduleExecutorService
            .scheduleAtFixedRate(this::doCompensate, handlerProperties.getSchedulePeriod(),
                handlerProperties.getMaxExecuteTimeout(), TimeUnit.SECONDS);
    }

    public void doTrigger() {
        List<ImportTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    public void doReaperTrigger() {
        List<ImportTriggerEntry> registerIdList = triggerService
            .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getMinReaperSeconds(), handlerProperties.getTriggerOffset());
        doActualTrigger(registerIdList);
    }

    public void doCompensate() {
        log.info("[ScheduleTriggerAsyncImportHandler#doCompensate] start...");
        try {
            triggerService.handleExpirationTrigger(handlerProperties.getMaxExecuteTimeout());
            triggerService.archiveHistoryTrigger(handlerProperties.getMaxArchiveHours(), handlerProperties.getMaxTriggerCount());
        } catch (Exception ex) {
            log.error("[ScheduleTriggerAsyncImportHandler#doCompensate] error!...", ex);
        }
        log.info("[ScheduleTriggerAsyncImportHandler#doCompensate] end...");
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
                .forEach(this::doTriggerSingle);
        } catch (Exception ex) {
            log.error("[ScheduleTriggerAsyncImportHandler#doTrigger] error!", ex);
        }
        log.info("[ScheduleTriggerAsyncImportHandler#doTrigger] end.....");
    }

    private void doTriggerSingle(ImportTriggerEntry entry) {
        boolean execute = triggerService.startExecute(entry.getRegisterId(), entry.getTriggerCount());
        if (execute) {
            try {
                ImportRecordInfo recordInfo = storageService.getImportRecord(entry.getRegisterId());
                BaseAsyncImportExecutor<?> executor = FileImportExecutorSupport.get(recordInfo.getImportCode());
                
                asyncImportHandler.execute(executor, recordInfo.getFileUrl(), entry.getRegisterId());
                
                triggerService.exeSuccess(entry.getRegisterId());
            } catch (Exception ex) {
                log.error("[ScheduleTriggerAsyncImportHandler#doTriggerSingle] execute-failed! registerId:{}",
                    entry.getRegisterId(), ex);
                triggerService.exeFail(entry.getRegisterId());
            }
        }
    }
}
