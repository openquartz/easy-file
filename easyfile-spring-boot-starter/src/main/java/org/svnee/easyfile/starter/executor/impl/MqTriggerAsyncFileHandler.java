package org.svnee.easyfile.starter.executor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.trigger.DownloadTriggerMessage;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerHandler;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerProducer;
import org.svnee.easyfile.starter.spring.boot.autoconfig.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.MqAsyncHandlerProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * RocketMQ Trigger AsyncFileHandler
 *
 * @author svnee
 **/
@Slf4j
public class MqTriggerAsyncFileHandler extends DatabaseAsyncFileHandlerAdapter implements MQTriggerHandler {

    private final DownloadTriggerService triggerMessage;
    private final MQTriggerProducer mqTriggerProducer;

    public MqTriggerAsyncFileHandler(
        EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        DownloadTriggerService triggerMessage,
        MqAsyncHandlerProperties handlerProperties,
        MQTriggerProducer mqProducer) {
        super(downloadProperties, uploadService, storageService, triggerMessage, handlerProperties);
        this.triggerMessage = triggerMessage;
        this.mqTriggerProducer = mqProducer;
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        super.execute(executor, baseRequest, registerId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //发送消息
                DownloadTriggerMessage triggerMessage = new DownloadTriggerMessage();
                triggerMessage.setRegisterId(registerId);
                triggerMessage.setTriggerTimestamp(System.currentTimeMillis());
                mqTriggerProducer.send(triggerMessage);
            }
        });
    }

    @Override
    public void handle(org.svnee.easyfile.starter.executor.trigger.DownloadTriggerMessage message) {

    }
}
