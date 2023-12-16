package com.openquartz.easyfile.starter.trigger.handler;

import com.openquartz.easyfile.common.bean.IRequest;
import com.openquartz.easyfile.core.exception.ExportRejectExecuteException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.openquartz.easyfile.core.executor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.DefaultAsyncHandlerThreadPoolProperties;

/**
 * 默认异步文件处理器实现
 * <p>
 * 默认采用线程池实现。如果数量任务超限将抛出异常{@link ExportRejectExecuteException}
 * 默认线程池配置{@link DefaultAsyncHandlerThreadPoolProperties} 可以进行自行覆盖
 * 可以做异常捕捉做自己的提示执行
 *
 * @author svnee
 */

public class DefaultAsyncFileTriggerExecuteHandler implements AsyncFileTriggerExecuteHandler {

    private final ExecutorService executorService;
    private final AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncFileTriggerExecuteHandler.class);

    public ExecutorService init(DefaultAsyncHandlerThreadPoolProperties threadPoolConfig,
                                BaseDefaultRejectExecutionHandler rejectHandler) {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(threadPoolConfig.getMaxBlockingQueueSize());

        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                TimeUnit.SECONDS,
                blockingQueue,
                new ThreadFactoryBuilder().setNameFormat(threadPoolConfig.getThreadPrefix() + "-thread-%d").build(),
                rejectHandler);
    }

    public DefaultAsyncFileTriggerExecuteHandler(AsyncFileTriggerExecuteHandlerFactory asyncFileTriggerExecuteHandlerFactory,
                                                 BaseDefaultRejectExecutionHandler rejectExecutionHandler,
                                                 DefaultAsyncHandlerThreadPoolProperties threadPoolConfig) {
        this.executorService = init(threadPoolConfig, rejectExecutionHandler);
        this.asyncFileTriggerExecuteHandlerFactory = asyncFileTriggerExecuteHandlerFactory;
        log.info(">>>>>>[DefaultAsyncFileHandler] Init,thread-pool-config:{}", threadPoolConfig);
    }

    @Override
    @SuppressWarnings("all")
    public <T extends Executor, R extends IRequest> void execute(T executor, R baseRequest, Long registerId) {

        FileHandlerExecutor handlerExecutor = asyncFileTriggerExecuteHandlerFactory.get(executor.getClass(), baseRequest.getClass());

        executorService.execute(() -> handlerExecutor.execute(executor, baseRequest, registerId));
    }

}
