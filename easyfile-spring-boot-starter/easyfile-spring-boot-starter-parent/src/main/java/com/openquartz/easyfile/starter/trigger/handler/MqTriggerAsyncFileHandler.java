package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.core.executor.impl.DatabaseAsyncFileHandlerAdapter;
import com.openquartz.easyfile.core.executor.trigger.ExportTriggerMessage;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.MqAsyncHandlerProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * RocketMQ Trigger AsyncFileHandler
 *
 * @author svnee
 **/
@Slf4j
public class MqTriggerAsyncFileHandler extends DatabaseAsyncFileHandlerAdapter implements MQTriggerHandler {

    private final FileTriggerService triggerService;
    private final MQTriggerProducer mqTriggerProducer;
    private final MqAsyncHandlerProperties handlerProperties;

    public MqTriggerAsyncFileHandler(
        EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        FileTriggerService triggerService,
        MqAsyncHandlerProperties handlerProperties,
        MQTriggerProducer mqProducer) {

        super(downloadProperties, uploadService, storageService, triggerService, handlerProperties);
        this.triggerService = triggerService;
        this.mqTriggerProducer = mqProducer;
        this.handlerProperties = handlerProperties;
    }

    @Override
    public void execute(BaseExportExecutor executor, BaseExportRequestContext baseRequest, Long registerId) {
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
        ExportTriggerMessage triggerMessage = new ExportTriggerMessage();
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
    public void handle(ExportTriggerMessage message) {
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
