package org.svnee.easyfile.starter.executor.impl;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.request.DownloadTriggerRequest;
import org.svnee.easyfile.common.response.DownloadTriggerEntry;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.processor.FileExportExecutorSupport;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.ScheduleAsyncHandlerProperties;
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

    private ScheduledThreadPoolExecutor init(ScheduleAsyncHandlerProperties handlerProperties,
        BaseDefaultDownloadRejectExecutionHandler rejectHandler) {
        return new ScheduledThreadPoolExecutor(handlerProperties.getThreadPoolCorePoolSize(),
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
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        DownloadTriggerRequest triggerRequest = new DownloadTriggerRequest();
        triggerRequest.setRegisterId(registerId);
        triggerService.trigger(triggerRequest);
    }

    public void doTrigger() {
        log.info("[ScheduleTriggerAsyncFileHandler#doTrigger] start.....");
        try {
            List<DownloadTriggerEntry> registerIdList = triggerService
                .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                    handlerProperties.getTriggerOffset());
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
    }

}
