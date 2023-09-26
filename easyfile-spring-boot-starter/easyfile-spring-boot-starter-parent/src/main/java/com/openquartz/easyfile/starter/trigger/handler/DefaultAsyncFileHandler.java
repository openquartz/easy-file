package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.bean.BaseExportRequestContext;
import com.openquartz.easyfile.core.exception.ExportRejectExecuteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.core.executor.AsyncFileHandlerAdapter;
import com.openquartz.easyfile.core.executor.BaseDefaultExportRejectExecutionHandler;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.file.UploadService;

/**
 * 默认异步文件处理器实现
 * <p>
 * 默认采用线程池实现。如果数量任务超限将抛出异常{@link ExportRejectExecuteException}
 * 默认线程池配置{@link DefaultAsyncHandlerThreadPoolProperties} 可以进行自行覆盖
 * 可以做异常捕捉做自己的提示执行
 *
 * @author svnee
 */
public class DefaultAsyncFileHandler extends AsyncFileHandlerAdapter {

    private final ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncFileHandler.class);

    public ExecutorService init(DefaultAsyncHandlerThreadPoolProperties threadPoolConfig,
        BaseDefaultExportRejectExecutionHandler rejectHandler) {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(threadPoolConfig.getMaxBlockingQueueSize());

        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
            threadPoolConfig.getMaximumPoolSize(),
            threadPoolConfig.getKeepAliveTime(),
            TimeUnit.SECONDS,
            blockingQueue,
            new ThreadFactoryBuilder().setNameFormat(threadPoolConfig.getThreadPrefix() + "-thread-%d").build(),
            rejectHandler);
    }

    public DefaultAsyncFileHandler(IEasyFileDownloadProperty downloadProperties,
        UploadService uploadService,
        DownloadStorageService storageService,
        BaseDefaultExportRejectExecutionHandler rejectExecutionHandler,
        DefaultAsyncHandlerThreadPoolProperties threadPoolConfig) {
        super(downloadProperties, uploadService, storageService);
        executorService = init(threadPoolConfig, rejectExecutionHandler);
        log.info(">>>>>>[DefaultAsyncFileHandler] Init,thread-pool-config:{}", threadPoolConfig);
    }

    @Override
    public void execute(BaseExportExecutor executor, BaseExportRequestContext baseRequest, Long registerId) {
        executorService.execute(() -> doExecute(executor, baseRequest, registerId));
    }
}
