package com.openquartz.easyfile.starter.trigger.retry;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadRetryScannerTest {

    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private BaseAsyncFileHandler handler;

    @Test
    public void scan_claimsAndReexecutesEachId() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Arrays.asList(1L, 2L));
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(downloadStorageService).claimRetry(1L, Constants.SYSTEM_USER);
        verify(downloadStorageService).claimRetry(2L, Constants.SYSTEM_USER);
        verify(handler).reexecute(1L);
        verify(handler).reexecute(2L);
    }

    @Test
    public void scan_continuesWhenOneIdThrows() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Arrays.asList(1L, 2L));
        doThrow(new RuntimeException("x")).when(handler).reexecute(1L);
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(handler).reexecute(2L);
    }

    @Test
    public void scan_swallowsListRetryableFailure() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30))
            .thenThrow(new RuntimeException("list failed"));
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(handler, never()).reexecute(anyLong());
        verify(downloadStorageService, never()).claimRetry(anyLong(), anyString());
    }

    @Test
    public void scan_noopWhenEmpty() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Collections.emptyList());
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(handler, never()).reexecute(anyLong());
        verify(downloadStorageService, never()).claimRetry(anyLong(), anyString());
    }
}
