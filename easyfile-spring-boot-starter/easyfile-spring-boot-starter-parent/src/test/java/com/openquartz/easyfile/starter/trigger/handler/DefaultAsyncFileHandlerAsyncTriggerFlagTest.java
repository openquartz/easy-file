package com.openquartz.easyfile.starter.trigger.handler;

import static org.junit.Assert.assertTrue;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.impl.DefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.support.FileExportTriggerContext;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.file.UploadService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncFileHandlerAsyncTriggerFlagTest {

    @Mock
    private IEasyFileDownloadProperty downloadProperty;
    @Mock
    private UploadService uploadService;
    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private BaseDownloadExecutor executor;
    @Mock
    private BaseDownloaderRequestContext baseRequest;

    @After
    public void tearDown() {
        FileExportTriggerContext.clear();
    }

    @Test
    public void execute_setsAsyncTriggerFlagOnWorkerThread() throws InterruptedException {
        DefaultAsyncHandlerThreadPoolProperties threadPoolConfig = new DefaultAsyncHandlerThreadPoolProperties();
        threadPoolConfig.setCorePoolSize(1);
        threadPoolConfig.setMaximumPoolSize(1);
        threadPoolConfig.setMaxBlockingQueueSize(1);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean asyncTriggerOnWorker = new AtomicBoolean(false);

        DefaultAsyncFileHandler handler = new DefaultAsyncFileHandler(downloadProperty, uploadService,
            downloadStorageService, new DefaultDownloadRejectExecutionHandler(), threadPoolConfig) {
            @Override
            public void doExecute(BaseDownloadExecutor exec, BaseDownloaderRequestContext request, Long registerId) {
                asyncTriggerOnWorker.set(FileExportTriggerContext.isAsyncTrigger());
                latch.countDown();
            }

            @Override
            protected void dispatchRetry(Long registerId) {
                // skip claim/retry for this flag-only assertion
            }
        };

        handler.execute(executor, baseRequest, 1L);

        assertTrue("worker did not run in time", latch.await(5, TimeUnit.SECONDS));
        assertTrue("async-trigger flag must be true on worker thread", asyncTriggerOnWorker.get());
    }
}
