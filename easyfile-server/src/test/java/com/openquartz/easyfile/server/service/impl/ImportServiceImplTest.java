package com.openquartz.easyfile.server.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.ImportResult;
import java.util.Collections;
import com.openquartz.easyfile.server.service.NotifyService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import com.openquartz.easyfile.storage.imports.ImportTriggerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * ImportServiceImpl facade tests.
 */
public class ImportServiceImplTest {

    private ImportStorageService importStorageService;
    private ImportTriggerService importTriggerService;
    private NotifyService notifyService;
    private ImportServiceImpl importService;

    @Before
    public void setUp() {
        importStorageService = mock(ImportStorageService.class);
        importTriggerService = mock(ImportTriggerService.class);
        notifyService = mock(NotifyService.class);
        importService = new ImportServiceImpl(importStorageService, importTriggerService, notifyService);
    }

    @Test
    public void registerImport_delegatesToStorageAndTriggers() {
        ImportRegisterRequest request = ImportRegisterRequest.builder()
            .appId("app")
            .importCode("import-code")
            .ossUrl("http://oss/file.xlsx")
            .fileName("file.xlsx")
            .fileType("xlsx")
            .createBy("u1")
            .enableAsync(true)
            .build();
        when(importStorageService.register(request)).thenReturn(5L);

        Long registerId = importService.registerImport(request);

        assertEquals(Long.valueOf(5L), registerId);
        verify(importStorageService).register(request);
        ArgumentCaptor<ImportTriggerRequest> triggerCaptor = ArgumentCaptor.forClass(ImportTriggerRequest.class);
        verify(importTriggerService).trigger(triggerCaptor.capture());
        assertEquals(Long.valueOf(5L), triggerCaptor.getValue().getRegisterId());
    }

    @Test
    public void registerImport_skipsTriggerWhenNotAsync() {
        ImportRegisterRequest request = ImportRegisterRequest.builder()
            .appId("app")
            .importCode("import-code")
            .ossUrl("http://oss/file.xlsx")
            .fileName("file.xlsx")
            .fileType("xlsx")
            .enableAsync(false)
            .build();
        when(importStorageService.register(request)).thenReturn(9L);

        assertEquals(Long.valueOf(9L), importService.registerImport(request));
        verify(importTriggerService, never()).trigger(any());
    }

    @Test
    public void queryImportStatus_readsFromStorageRequestInfo() {
        ImportRequestInfo info = new ImportRequestInfo();
        info.setImportStatus(ImportStatusEnum.EXECUTING);
        info.setExecuteProcess(40);
        info.setTotalCount(100L);
        info.setSuccessCount(40L);
        info.setFailCount(0L);
        when(importStorageService.getRequestInfoByRegisterId(7L)).thenReturn(info);

        ImportResult result = importService.queryImportStatus(7L);

        assertEquals(Long.valueOf(7L), result.getRegisterId());
        assertEquals(ImportStatusEnum.EXECUTING.getCode(), result.getStatus());
        assertFalse(result.isSuccess());
        assertEquals(Integer.valueOf(40), result.getProcess());
    }

    @Test
    public void complete_notifiesWhenEnableNotifyAndTerminal() {
        ImportRequestInfo info = new ImportRequestInfo();
        ImportRegisterRequest registerRequest = ImportRegisterRequest.builder()
            .enableNotify(true)
            .notifyEmail("a@b.com")
            .createBy("op")
            .importCode("code")
            .build();
        info.setRegisterRequest(registerRequest);
        info.setImportCode("code");
        when(importStorageService.getRequestInfoByRegisterId(3L)).thenReturn(info);

        importService.complete(3L, ImportStatusEnum.SUCCESS, 10L, 10L, 0L, 10L, null);

        verify(importStorageService).complete(eq(3L), eq(ImportStatusEnum.SUCCESS), eq(10L), eq(10L), eq(0L),
            eq(10L), eq(null));
        verify(notifyService).notify(any(), any());
    }

    @Test
    public void retryImport_delegatesToStorageAndRetriggers() {
        when(importStorageService.retry(1L, "op")).thenReturn(true);
        assertTrue(importService.retryImport(1L, "op"));
        verify(importStorageService).retry(1L, "op");
        ArgumentCaptor<ImportTriggerRequest> triggerCaptor = ArgumentCaptor.forClass(ImportTriggerRequest.class);
        verify(importTriggerService).trigger(triggerCaptor.capture());
        assertEquals(Long.valueOf(1L), triggerCaptor.getValue().getRegisterId());
    }

    @Test
    public void retryImport_skipsTriggerWhenRetryFails() {
        when(importStorageService.retry(2L, "op")).thenReturn(false);
        assertFalse(importService.retryImport(2L, "op"));
        verify(importTriggerService, never()).trigger(any());
    }

    @Test
    public void autoRegisterTask_delegatesToStorage() {
        AutoTaskRegisterRequest request = new AutoTaskRegisterRequest()
            .setAppId("app")
            .setUnifiedAppId("u-app")
            .setImportCodeMap(Collections.singletonMap("import-code", "desc"));

        importService.autoRegisterTask(request);

        verify(importStorageService).autoRegisterTask(request);
    }
}
