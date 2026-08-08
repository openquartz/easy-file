package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.executor.AsyncImportHandlerAdapter;
import com.openquartz.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncImportHandlerThreadPoolProperties;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认异步导入处理器（线程池）
 *
 * @author svnee
 */
public class DefaultAsyncImportHandler extends AsyncImportHandlerAdapter {

    private final ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncImportHandler.class);

    public ExecutorService init(DefaultAsyncImportHandlerThreadPoolProperties threadPoolConfig,
        BaseDefaultDownloadRejectExecutionHandler rejectHandler) {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(threadPoolConfig.getMaxBlockingQueueSize());
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
            threadPoolConfig.getMaximumPoolSize(),
            threadPoolConfig.getKeepAliveTime(),
            TimeUnit.SECONDS,
            blockingQueue,
            new ThreadFactoryBuilder().setNameFormat(threadPoolConfig.getThreadPrefix() + "-thread-%d").build(),
            rejectHandler);
    }

    public DefaultAsyncImportHandler(
        ImportStorageService importStorageService,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler,
        DefaultAsyncImportHandlerThreadPoolProperties threadPoolConfig) {
        this(importStorageService, null, rejectExecutionHandler, threadPoolConfig);
    }

    public DefaultAsyncImportHandler(
        ImportStorageService importStorageService,
        IdempotentImportManager idempotentImportManager,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler,
        DefaultAsyncImportHandlerThreadPoolProperties threadPoolConfig) {
        super(importStorageService, idempotentImportManager);
        executorService = init(threadPoolConfig, rejectExecutionHandler);
        log.info(">>>>>>[DefaultAsyncImportHandler] Init,thread-pool-config:{}", threadPoolConfig);
    }

    @Override
    public void execute(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId) {
        executorService.execute(() -> doExecute(executor, context, registerId));
    }
}
