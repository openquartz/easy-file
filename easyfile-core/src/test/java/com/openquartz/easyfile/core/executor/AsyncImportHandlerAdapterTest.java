package com.openquartz.easyfile.core.executor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import org.junit.Before;
import org.junit.Test;

/**
 * AsyncImportHandlerAdapter unit tests.
 */
public class AsyncImportHandlerAdapterTest {

    private ImportStorageService importStorageService;
    private BaseImportExecutor<?> executor;
    private IdempotentImportManager idempotentImportManager;
    private AsyncImportHandlerAdapter adapter;

    @Before
    public void setUp() {
        importStorageService = mock(ImportStorageService.class);
        executor = mock(BaseImportExecutor.class);
        idempotentImportManager = mock(IdempotentImportManager.class);
        adapter = new AsyncImportHandlerAdapter(importStorageService, idempotentImportManager) {
        };
    }

    @Test
    public void handleResult_enableRunningFalse_skipsProcess() {
        when(importStorageService.enableRunning(1L)).thenReturn(false);

        ImportRequestContext context = new ImportRequestContext();
        context.setRegisterId(1L);
        context.setSourceFileUrl("http://oss/file.xlsx");
        context.setFileName("file.xlsx");
        context.setFileType("xlsx");

        ImportProcessResult result = adapter.handleResult(executor, context, 1L);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        verify(executor, never()).process(any(ImportContext.class));
        verify(importStorageService, never()).complete(anyLong(), any(ImportStatusEnum.class),
            anyLong(), anyLong(), anyLong(), anyLong(), anyString());
    }

    @Test
    public void handleResult_idempotentShortCircuit_beforeEnableRunning_completesStorage() {
        when(idempotentImportManager.isExecuted("biz-key")).thenReturn(true);
        when(idempotentImportManager.getExecutedResult("biz-key")).thenReturn(
            ImportResult.builder().success(true).status("success").totalCount(10L).successCount(10L).failCount(0L)
                .build());

        ImportRequestContext context = baseContext();
        context.setBusinessUniqueKey("biz-key");

        ImportProcessResult result = adapter.handleResult(executor, context, 1L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.isIdempotent());
        verify(importStorageService, never()).enableRunning(anyLong());
        verify(executor, never()).process(any(ImportContext.class));
        verify(importStorageService).complete(eq(1L), eq(ImportStatusEnum.SUCCESS), eq(10L), eq(10L), eq(0L),
            eq(10L), nullable(String.class));
        verify(executor).asyncCompleteCallback(eq(result), eq(context));
    }

    @Test
    public void handleResult_processThrowsAfterEnableRunning_callsComplete() {
        when(importStorageService.enableRunning(1L)).thenReturn(true);
        when(executor.process(any(ImportContext.class))).thenThrow(new RuntimeException("boom"));

        ImportRequestContext context = baseContext();

        ImportProcessResult result = adapter.handleResult(executor, context, 1L);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        verify(importStorageService).complete(eq(1L), eq(ImportStatusEnum.FAIL), eq(0L), eq(0L), eq(0L), eq(0L),
            eq("boom"));
        verify(executor).asyncCompleteCallback(eq(result), eq(context));
    }

    @Test
    public void handleResult_processReturnsNullAfterEnableRunning_callsComplete() {
        when(importStorageService.enableRunning(1L)).thenReturn(true);
        when(executor.process(any(ImportContext.class))).thenReturn(null);

        ImportRequestContext context = baseContext();

        ImportProcessResult result = adapter.handleResult(executor, context, 1L);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        verify(importStorageService).complete(eq(1L), eq(ImportStatusEnum.FAIL), eq(0L), eq(0L), eq(0L), eq(0L),
            eq("import process returned null"));
        verify(executor).asyncCompleteCallback(eq(result), eq(context));
    }

    private static ImportRequestContext baseContext() {
        ImportRequestContext context = new ImportRequestContext();
        context.setRegisterId(1L);
        context.setSourceFileUrl("http://oss/file.xlsx");
        context.setFileName("file.xlsx");
        context.setFileType("xlsx");
        return context;
    }
}
