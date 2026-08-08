package com.openquartz.easyfile.core.executor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import org.junit.Before;
import org.junit.Test;

/**
 * AsyncImportHandlerAdapter unit tests.
 */
public class AsyncImportHandlerAdapterTest {

    private ImportStorageService importStorageService;
    private BaseImportExecutor<?> executor;
    private AsyncImportHandlerAdapter adapter;

    @Before
    public void setUp() {
        importStorageService = mock(ImportStorageService.class);
        executor = mock(BaseImportExecutor.class);
        adapter = new AsyncImportHandlerAdapter(importStorageService) {
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
    }
}
