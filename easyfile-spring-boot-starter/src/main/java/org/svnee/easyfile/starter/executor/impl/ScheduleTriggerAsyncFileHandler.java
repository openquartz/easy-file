package org.svnee.easyfile.starter.executor.impl;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.DownloadRequestInfo;
import org.svnee.easyfile.common.request.DownloadTriggerRequest;
import org.svnee.easyfile.common.response.DownloadTriggerResult;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.common.util.CollectionUtils;
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
public class ScheduleTriggerAsyncFileHandler extends AsyncFileHandlerAdapter implements InitializingBean {

    private final DownloadTriggerService downloadTriggerService;
    private final DownloadStorageService downloadStorageService;
    private final ScheduledThreadPoolExecutor scheduleExecutorService;
    private final ScheduleAsyncHandlerProperties handlerProperties;

    private final ScheduledThreadPoolExecutor compensateScheduleExecutorService = new ScheduledThreadPoolExecutor(1,
        new ThreadFactoryBuilder()
            .setNameFormat("CompensateScheduleAsyncHandler-thread-%d")
            .build());

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
        DownloadTriggerService downloadTriggerService,
        ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler) {
        super(downloadProperties, uploadService, storageService);
        this.downloadTriggerService = downloadTriggerService;
        this.handlerProperties = scheduleAsyncHandlerProperties;
        this.scheduleExecutorService = init(scheduleAsyncHandlerProperties, rejectExecutionHandler);
        this.downloadStorageService = storageService;
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        DownloadTriggerRequest triggerRequest = new DownloadTriggerRequest();
        triggerRequest.setRegisterId(registerId);
        downloadTriggerService.trigger(triggerRequest);
    }

    public void doTrigger() {
        List<DownloadTriggerResult> registerIdList = downloadTriggerService
            .getTriggerRegisterId(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getTriggerOffset());
        if (CollectionUtils.isEmpty(registerIdList)) {
            return;
        }
        registerIdList.forEach(k -> {
            boolean execute = downloadTriggerService.startExecute(k.getRegisterId(), k.getTriggerCount());
            if (execute) {
                DownloadRequestInfo requestInfo = downloadStorageService.getRequestInfoByRegisterId(k.getRegisterId());
                try {
                    doExecute(FileExportExecutorSupport.get(requestInfo.getDownloadCode()),
                        requestInfo.getRequestContext(), k.getRegisterId());
                    downloadTriggerService.exeSuccess(k.getRegisterId());
                } catch (Exception ex) {
                    downloadTriggerService.exeFail(k.getRegisterId());
                }
            }
        });
    }

    private void doCompensate() {
        downloadTriggerService.handleExpirationTrigger(handlerProperties.getMaxExecuteTimeout());
    }

    @Override
    public void afterPropertiesSet() {

        compensateScheduleExecutorService
            .scheduleAtFixedRate(this::doCompensate, handlerProperties.getSchedulePeriod(),
                handlerProperties.getMaxExecuteTimeout(), TimeUnit.SECONDS);

        double initDelaySeconds = new Random(1).nextDouble() * handlerProperties.getSchedulePeriod();
        scheduleExecutorService
            .scheduleAtFixedRate(this::doTrigger, (int) initDelaySeconds, handlerProperties.getSchedulePeriod(),
                TimeUnit.SECONDS);
    }

}
