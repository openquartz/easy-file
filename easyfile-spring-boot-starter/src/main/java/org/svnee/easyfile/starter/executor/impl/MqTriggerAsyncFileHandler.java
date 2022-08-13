package org.svnee.easyfile.starter.executor.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.response.DownloadTriggerEntry;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.executor.trigger.DownloadTriggerMessage;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerHandler;
import org.svnee.easyfile.starter.executor.trigger.MQTriggerProducer;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.MqAsyncHandlerProperties;
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

    private final DownloadTriggerService triggerService;
    private final MQTriggerProducer mqTriggerProducer;
    private final MqAsyncHandlerProperties handlerProperties;

    public MqTriggerAsyncFileHandler(
        EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        DownloadTriggerService triggerService,
        MqAsyncHandlerProperties handlerProperties,
        MQTriggerProducer mqProducer) {
        super(downloadProperties, uploadService, storageService, triggerService, handlerProperties);
        this.triggerService = triggerService;
        this.mqTriggerProducer = mqProducer;
        this.handlerProperties = handlerProperties;
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        super.execute(executor, baseRequest, registerId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    doSend(registerId);
                } catch (Exception ex) {
                    log.error("[MqTriggerAsyncFileHandler#execute] doSend,Error! registerId:{}", registerId, ex);
                }
            }
        });
    }

    private void doSend(Long registerId) {
        //发送消息
        DownloadTriggerMessage triggerMessage = new DownloadTriggerMessage();
        triggerMessage.setRegisterId(registerId);
        triggerMessage.setTriggerTimestamp(System.currentTimeMillis());
        boolean send = mqTriggerProducer.send(triggerMessage);
        if (send) {
            triggerService.enterWaiting(registerId);
        }
    }

    @Override
    public void doCompensate() {

        // 补偿-归档过期部分数据
        super.doCompensate();

        // 处理超时等待过期触发器
        triggerService.handleWaitingExpirationTrigger(handlerProperties.getMaxWaitingTimeout());

        // 查询需要回溯的补偿的触发器
        List<DownloadTriggerEntry> entryList = triggerService
            .getTriggerCompensate(handlerProperties.getLookBackHours(), handlerProperties.getMaxTriggerCount(),
                handlerProperties.getOffset());

        entryList.stream()
            .sorted(Comparator.comparing(DownloadTriggerEntry::getRegisterId))
            .forEach((k -> doSend(k.getRegisterId())));
    }

    @Override
    public void handle(DownloadTriggerMessage message) {
        // 查詢
        DownloadTriggerEntry triggerEntry = triggerService
            .getTriggerRegisterId(message.getRegisterId(), handlerProperties.getMaxTriggerCount());
        if (Objects.isNull(triggerEntry)) {
            return;
        }
        try {
            super.doTrigger(triggerEntry);
        } catch (Exception ex) {
            log.error("[MqTriggerAsyncFileHandler#handle]message:{}", message, ex);
        }
    }
}
