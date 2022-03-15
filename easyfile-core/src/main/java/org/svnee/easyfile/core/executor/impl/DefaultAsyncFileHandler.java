package org.svnee.easyfile.core.executor.impl;

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svnee.easyfile.common.bean.BaseDownloaderRequestContext;
import org.svnee.easyfile.common.bean.PageTotalContext;
import org.svnee.easyfile.common.response.ExportResult;
import org.svnee.easyfile.core.config.DefaultAsyncHandlerThreadPoolConfig;
import org.svnee.easyfile.core.executor.BaseDefaultDownloadRejectExecutionHandler;
import org.svnee.easyfile.core.executor.BaseDownloadExecutor;
import org.svnee.easyfile.core.intercept.DownloadExecutorInterceptor;
import org.svnee.easyfile.core.intercept.ExecutorInterceptorSupport;
import org.svnee.easyfile.core.intercept.InterceptorContext;

/**
 * 默认异步文件处理器实现
 * <p>
 * 默认采用线程池实现。如果数量任务超限将抛出异常{@link org.svnee.easyfile.core.exception.DownloadRejectExecuteException}
 * 默认线程池配置{@link DefaultAsyncHandlerThreadPoolConfig} 可以进行自行覆盖
 * 可以做异常捕捉做自己的提示执行
 *
 * @author svnee
 */
public class DefaultAsyncFileHandler extends AsyncFileHandlerAdapter {

    private final ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncFileHandler.class);

    public ExecutorService init(DefaultAsyncHandlerThreadPoolConfig threadPoolConfig,
        BaseDefaultDownloadRejectExecutionHandler rejectHandler) {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(threadPoolConfig.getMaxBlockingQueueSize());

        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
            threadPoolConfig.getMaximumPoolSize(),
            threadPoolConfig.getKeepAliveTime(),
            TimeUnit.SECONDS,
            blockingQueue,
            rejectHandler);
    }

    public DefaultAsyncFileHandler(AdsProperties adsProperties,
        AdsConfig adsConfig,
        FileService fileService,
        BaseDefaultDownloadRejectExecutionHandler rejectExecutionHandler,
        DefaultAsyncHandlerThreadPoolConfig threadPoolConfig) {
        super(adsProperties, adsConfig, fileService);
        executorService = init(threadPoolConfig, rejectExecutionHandler);
        log.info(">>>>>>[DefaultAsyncFileHandler] Init,thread-pool-config:{}", threadPoolConfig);
    }

    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        executorService.execute(() -> {
            log.info("[DefaultAsyncFileHandler#execute]start,execute!registerId:{}", registerId);
            ExportResult result = null;
            InterceptorContext interceptorContext = InterceptorContext.newInstance();
            try {
                // 执行拦截前置处理
                beforeHandle(executor, baseRequest, registerId, interceptorContext);
                // 执行拦截
                result = handleResult(executor, baseRequest, registerId);
            } catch (Exception ex) {
                log.error("[DefaultAsyncFileHandler#execute]end,execute error!registerId:{}", registerId, ex);
                throw ex;
            } finally {
                // 执行拦截后置处理
                afterHandle(executor, baseRequest, result, interceptorContext);
                PageTotalContext.clear();
            }
            log.info("[DefaultAsyncFileHandler#execute]end,execute!registerId:{}", registerId);
        });
    }

    private void afterHandle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest,
        ExportResult result, InterceptorContext interceptorContext) {
        ExecutorInterceptorSupport.getInterceptors().stream()
            .sorted(((o1, o2) -> o2.order() - o1.order()))
            .forEach(interceptor -> interceptor.afterExecute(executor, baseRequest, result, interceptorContext));
    }

    private void beforeHandle(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest,
        Long registerId, InterceptorContext interceptorContext) {
        ExecutorInterceptorSupport.getInterceptors().stream()
            .sorted((Comparator.comparingInt(DownloadExecutorInterceptor::order)))
            .forEach(interceptor -> interceptor.beforeExecute(executor, baseRequest, registerId, interceptorContext));
    }
}
