package com.openquartz.easyfile.starter.trigger.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.trigger.DownloadTriggerMessage;
import com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.RocketMqAsyncHandlerProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.DownloadTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(MockitoJUnitRunner.class)
public class MqTriggerAsyncFileHandlerReexecuteTest {

    @Mock
    private UploadService uploadService;
    @Mock
    private DownloadStorageService storageService;
    @Mock
    private DownloadTriggerService triggerService;
    @Mock
    private MQTriggerProducer mqTriggerProducer;
    @Mock
    private BaseDownloadExecutor executor;
    @Mock
    private BaseDownloaderRequestContext requestContext;

    @After
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void reexecute_retriggersAndSends() {
        EasyFileDownloadProperties downloadProperties = new EasyFileDownloadProperties();
        downloadProperties.setAppId("demo");
        when(mqTriggerProducer.send(any(DownloadTriggerMessage.class))).thenReturn(true);

        MqTriggerAsyncFileHandler handler = new MqTriggerAsyncFileHandler(downloadProperties, uploadService,
            storageService, triggerService, new RocketMqAsyncHandlerProperties(), mqTriggerProducer);

        handler.reexecute(21L);

        verify(triggerService).retrigger(21L);
        verify(mqTriggerProducer).send(any(DownloadTriggerMessage.class));
        verify(triggerService).enterWaiting(21L);
    }

    @Test
    public void execute_stillSendsAfterCommit() {
        EasyFileDownloadProperties downloadProperties = new EasyFileDownloadProperties();
        downloadProperties.setAppId("demo");
        when(mqTriggerProducer.send(any(DownloadTriggerMessage.class))).thenReturn(true);

        MqTriggerAsyncFileHandler handler = new MqTriggerAsyncFileHandler(downloadProperties, uploadService,
            storageService, triggerService, new RocketMqAsyncHandlerProperties(), mqTriggerProducer);

        TransactionSynchronizationManager.initSynchronization();
        handler.execute(executor, requestContext, 22L);

        verify(triggerService).trigger(any());
        verify(mqTriggerProducer, never()).send(any(DownloadTriggerMessage.class));

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(mqTriggerProducer).send(any(DownloadTriggerMessage.class));
    }
}
