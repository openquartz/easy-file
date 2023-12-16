package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.bean.IRequest;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.core.executor.AsyncFileTriggerExecuteHandlerFactory;
import com.openquartz.easyfile.core.executor.Executor;
import com.openquartz.easyfile.core.executor.impl.DatabaseDefaultAsyncFileExportHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerHandler;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.core.executor.trigger.TriggerMessage;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.MqAsyncHandlerProperties;
import com.openquartz.easyfile.starter.util.TransactionProxyUtils;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * RocketMQ Trigger AsyncFileHandler
 *
 * @author svnee
 **/
@Slf4j
public class MqTriggerDefaultAsyncFileExportHandler extends DatabaseDefaultAsyncFileExportHandler implements MQTriggerHandler {

    private final FileTriggerService triggerService;
    private final MQTriggerProducer mqTriggerProducer;
    private final MqAsyncHandlerProperties handlerProperties;

    public MqTriggerDefaultAsyncFileExportHandler(
            DownloadStorageService storageService,
            FileTriggerService triggerService,
            MqAsyncHandlerProperties handlerProperties,
            MQTriggerProducer mqProducer,
            AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory) {

        super(storageService, triggerService, handlerProperties, asyncFileTriggerExecuteHandlerFactory);
        this.triggerService = triggerService;
        this.mqTriggerProducer = mqProducer;
        this.handlerProperties = handlerProperties;
    }

    @Override
    public <T extends Executor, R extends IRequest> void execute(T executor, R baseRequest, Long registerId) {

        super.execute(executor, baseRequest, registerId);

        TransactionProxyUtils.doAfterCommit(() -> {
            try {
                doSend(registerId);
            } catch (Exception ex) {
                log.error("[MqTriggerAsyncFileHandler#execute] doSend,Error! registerId:{}", registerId, ex);
            }
        });
    }

    private void doSend(Long registerId) {
        //发送消息
        TriggerMessage triggerMessage = new TriggerMessage();
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
    public void handle(TriggerMessage message) {
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
