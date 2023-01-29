package org.svnee.easyfile.starter.executor.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.thread.ThreadFactoryBuilder;
import org.svnee.easyfile.starter.executor.AsyncFileHandlerAdapter;
import org.svnee.easyfile.starter.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.file.UploadService;

/**
 * 默认异步文件处理器实现
 * <p>
 * 默认采用线程池实现。如果数量任务超限将抛出异常{@link org.svnee.easyfile.starter.exception.DownloadRejectExecuteException}
 * 默认线程池配置{@link DefaultAsyncHandlerThreadPoolProperties} 可以进行自行覆盖
 * 可以做异常捕捉做自己的提示执行
 *
 * @author svnee
 */
public class DefaultAsyncFileHandler extends AsyncFileHandlerAdapter {

    private final ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncFileHandler.class);

    public ExecutorService init(DefaultAsyncHandlerThreadPoolProperties threadPoolConfig,
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

    public DefaultAsyncFileHandler(EasyFileDownloadProperties downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler,
        DefaultAsyncHandlerThreadPoolProperties threadPoolConfig) {
        super(downloadProperties, uploadService, storageService);
        executorService = init(threadPoolConfig, rejectExecutionHandler);
        log.info(">>>>>>[DefaultAsyncFileHandler] Init,thread-pool-config:{}", threadPoolConfig);
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        executorService.execute(() -> doExecute(executor, baseRequest, registerId));
    }
}
