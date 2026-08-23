package com.openquartz.easyfile.starter.aop;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.core.executor.support.FileExportTriggerContext;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.LimitingService;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class FileExportInterceptorAsyncTriggerTest {

    @Mock
    private EasyFileDownloadProperties downloadProperties;
    @Mock
    private LimitingService limitingService;
    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private BaseAsyncFileHandler handler;
    @Mock
    private ApplicationContext context;
    @Mock
    private MethodInvocation invocation;

    @After
    public void tearDown() {
        FileExportTriggerContext.clear();
    }

    @Test
    public void invoke_whenAsyncTrigger_proceedsWithoutRegister() throws Throwable {
        FileExportTriggerContext.setAsyncTriggerFlagIfAbsent(true);
        when(invocation.proceed()).thenReturn("ok");

        FileExportInterceptor interceptor = new FileExportInterceptor(downloadProperties, limitingService,
            downloadStorageService, handler, context);

        interceptor.invoke(invocation);

        verify(invocation).proceed();
        verify(downloadStorageService, never()).register(any());
    }
}
