package com.openquartz.easyfile.core.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.file.UploadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncFileHandlerAdapterDispatchRetryTest {

    @Mock
    private IEasyFileDownloadProperty downloadProperties;
    @Mock
    private UploadService uploadService;
    @Mock
    private DownloadStorageService downloadStorageService;

    private RecordingHandler handler;
    private StubExecutor executor;

    @Before
    public void setUp() {
        handler = new RecordingHandler(downloadProperties, uploadService, downloadStorageService);
        executor = new StubExecutor();
        FileExportExecutorSupport.register("retryDispatchDemo",
            StubExecutor.class.getAnnotation(FileExportExecutor.class), executor);
    }

    @Test
    public void dispatchRetry_claimsThenReexecutes() {
        DownloadRequestInfo info = new DownloadRequestInfo();
        info.setDownloadCode("retryDispatchDemo");
        info.setRequestContext(new BaseDownloaderRequestContext());
        when(downloadStorageService.claimRetry(11L, Constants.SYSTEM_USER)).thenReturn(true);
        when(downloadStorageService.getRequestInfoByRegisterId(11L)).thenReturn(info);

        handler.dispatchRetry(11L);

        assertEquals(1, handler.executeCount);
        verify(downloadStorageService).claimRetry(11L, Constants.SYSTEM_USER);
    }

    @Test
    public void dispatchRetry_skipsReexecuteWhenClaimFails() {
        when(downloadStorageService.claimRetry(11L, Constants.SYSTEM_USER)).thenReturn(false);

        handler.dispatchRetry(11L);

        assertEquals(0, handler.executeCount);
        verify(downloadStorageService, never()).getRequestInfoByRegisterId(any());
    }

    @Test
    public void dispatchRetry_swallowsClaimException() {
        when(downloadStorageService.claimRetry(eq(11L), any())).thenThrow(new RuntimeException("boom"));
        handler.dispatchRetry(11L);
        assertEquals(0, handler.executeCount);
    }

    @Test
    public void reexecute_skipsWhenExecutorMissing() {
        DownloadRequestInfo info = new DownloadRequestInfo();
        info.setDownloadCode("missing-code");
        when(downloadStorageService.getRequestInfoByRegisterId(12L)).thenReturn(info);
        handler.reexecute(12L);
        assertEquals(0, handler.executeCount);
        assertFalse(FileExportExecutorSupport.contains("missing-code"));
    }

    private static final class RecordingHandler extends AsyncFileHandlerAdapter {
        private int executeCount;

        private RecordingHandler(IEasyFileDownloadProperty p, UploadService u, DownloadStorageService s) {
            super(p, u, s);
        }

        @Override
        public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
            executeCount++;
        }
    }

    @FileExportExecutor("retryDispatchDemo")
    private static final class StubExecutor implements BaseDownloadExecutor {
        @Override
        public boolean enableAsync(BaseDownloaderRequestContext context) {
            return true;
        }

        @Override
        public void export(com.openquartz.easyfile.common.bean.DownloaderRequestContext context) {
        }
    }
}
