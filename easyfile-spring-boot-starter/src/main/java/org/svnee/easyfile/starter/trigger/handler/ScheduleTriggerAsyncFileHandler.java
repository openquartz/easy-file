package org.svnee.easyfile.starter.trigger.handler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyfile.common.response.DownloadTriggerEntry;
import org.svnee.easyfile.common.concurrent.ThreadFactoryBuilder;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.core.executor.impl.DatabaseAsyncFileHandlerAdapter;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * 定时调度的触发异步文件下载处理器
 *
 * @author svnee
 **/
@Slf4j
public class ScheduleTriggerAsyncFileHandler extends DatabaseAsyncFileHandlerAdapter implements InitializingBean {

    private final DownloadTriggerService triggerService;
    private final ScheduledThreadPoolExecutor scheduleExecutorService;
    private final ScheduleAsyncHandlerProperties handlerProperties;
    private final ScheduledThreadPoolExecutor reaperScheduleExecutorService;

    private ScheduledThreadPoolExecutor init(ScheduleAsyncHandlerProperties handlerProperties,
        BaseDefaultDownloadRejectExecutionHandler rejectHandler) {
        return new ScheduledThreadPoolExecutor(handlerProperties.getThreadPoolCoreSize(),
            new ThreadFactoryBuilder()
                .setNameFormat(handlerProperties.getThreadPoolThreadPrefix() + "-thread-%d")
                .build(),
            rejectHandler);
    }

    public ScheduleTriggerAsyncFileHandler(
        EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        DownloadTriggerService triggerService,
        ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler) {
        super(downloadProperties, uploadService, storageService, triggerService, scheduleAsyncHandlerProperties);
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
