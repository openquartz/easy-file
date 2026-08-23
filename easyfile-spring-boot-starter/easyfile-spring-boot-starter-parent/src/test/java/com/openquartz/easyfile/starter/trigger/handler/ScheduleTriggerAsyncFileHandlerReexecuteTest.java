package com.openquartz.easyfile.starter.trigger.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.DownloadTriggerService;
import com.openquartz.easyfile.storage.file.UploadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleTriggerAsyncFileHandlerReexecuteTest {

    @Mock
    private UploadService uploadService;
    @Mock
    private DownloadStorageService storageService;
    @Mock
    private DownloadTriggerService triggerService;

    @Test
    public void reexecute_retriggersAndEnqueuesWithoutDoTriggerOnStack() {
        EasyFileDownloadProperties downloadProperties = new EasyFileDownloadProperties();
        downloadProperties.setAppId("demo");
        ScheduleAsyncHandlerProperties handlerProperties = new ScheduleAsyncHandlerProperties();
        DownloadTriggerEntry entry = DownloadTriggerEntry.of(11L, 0);
        when(triggerService.getTriggerRegisterId(eq(11L), eq(5))).thenReturn(entry);

        RecordingHandler handler = new RecordingHandler(downloadProperties, uploadService, storageService,
            triggerService, handlerProperties, (r, e) -> {
        });

        handler.reexecute(11L);

        verify(triggerService).retrigger(11L);
        verify(triggerService).getTriggerRegisterId(11L, 5);
        assertNotNull(handler.enqueued);
        assertEquals(Long.valueOf(11L), handler.enqueued.getRegisterId());
        verify(triggerService, never()).startExecute(anyLong(), anyInt());
        assertNull(handler.doTriggerOnStackEntry);
    }

    private static final class RecordingHandler extends ScheduleTriggerAsyncFileHandler {

        private DownloadTriggerEntry enqueued;
        private DownloadTriggerEntry doTriggerOnStackEntry;

        private RecordingHandler(EasyFileDownloadProperties downloadProperties, UploadService uploadService,
            DownloadStorageService storageService, DownloadTriggerService triggerService,
            ScheduleAsyncHandlerProperties scheduleAsyncHandlerProperties,
            BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler) {
            super(downloadProperties, uploadService, storageService, triggerService, scheduleAsyncHandlerProperties,
                rejectExecutionHandler);
        }

        @Override
        protected void enqueueDoTrigger(DownloadTriggerEntry entry) {
            enqueued = entry;
        }

        @Override
        public void doTrigger(DownloadTriggerEntry k) {
            doTriggerOnStackEntry = k;
            super.doTrigger(k);
        }
    }
}
