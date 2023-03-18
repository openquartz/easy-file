package org.svnee.easyfile.starter.aop;

import static org.svnee.easyfile.core.exception.DownloadErrorCode.FILE_GENERATOR_MUST_SUPPORT_ANNOTATION;
import static org.svnee.easyfile.core.exception.DownloadErrorCode.SYNC_DOWNLOAD_EXECUTE_ERROR;

import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.svnee.easyfile.core.annotations.FileExportExecutor;
import org.svnee.easyfile.common.bean.DownloaderRequestContext;
import org.svnee.easyfile.common.util.page.PageTotalContext;
import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.common.request.ExportLimitingRequest;
import org.svnee.easyfile.common.request.RegisterDownloadRequest;
import org.svnee.easyfile.common.util.IpUtil;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.common.exception.Asserts;
import org.svnee.easyfile.common.exception.EasyFileException;
import org.svnee.easyfile.core.executor.BaseAsyncFileHandler;
import org.svnee.easyfile.core.executor.BaseDownloadExecutor;
import org.svnee.easyfile.core.executor.BaseWrapperSyncResponseHeader;
import org.svnee.easyfile.core.intercept.listener.DownloadEndEvent;
import org.svnee.easyfile.core.intercept.listener.DownloadEndListener;
import org.svnee.easyfile.core.intercept.listener.DownloadStartEvent;
import org.svnee.easyfile.core.intercept.listener.DownloadStartListener;
import org.svnee.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import org.svnee.easyfile.storage.download.DownloadStorageService;
import org.svnee.easyfile.storage.download.LimitingService;

/**
 * 文件导出方法拦截器
 *
 * @author svnee
 **/
@Slf4j
public class FileExportInterceptor implements MethodInterceptor {

    private final EasyFileDownloadProperties downloadProperties;
    private final LimitingService limitingService;
    private final DownloadStorageService downloadStorageService;
    private final BaseAsyncFileHandler handler;
    private final ApplicationContext context;

    public FileExportInterceptor(EasyFileDownloadProperties downloadProperties,
        LimitingService limitingService,
        DownloadStorageService downloadStorageService,
        BaseAsyncFileHandler handler,
        ApplicationContext context) {
        this.downloadProperties = downloadProperties;
        this.limitingService = limitingService;
        this.downloadStorageService = downloadStorageService;
        this.handler = handler;
        this.context = context;
    }

    private static final String EXPORT_RESULT_METHOD_NAME = "exportResult";

    @Override
    public Object invoke(MethodInvocation invocation) {

        Object[] args = invocation.getArguments();
        BaseDownloadExecutor executor = (BaseDownloadExecutor) invocation.getThis();
        DownloaderRequestContext requestContext = (DownloaderRequestContext) args[0];
        Class<?> clazz = SpringContextUtil.getRealClass(executor);
        FileExportExecutor exportExecutor = clazz.getDeclaredAnnotation(FileExportExecutor.class);
        Asserts.notNull(exportExecutor, FILE_GENERATOR_MUST_SUPPORT_ANNOTATION);

        boolean async = false;
        // 是否 开启EasyFile异步服务
        try {
            if (downloadProperties.isEnabled()) {
                // 执行限流
                ExportLimitingRequest limitingRequest = buildLimitingRequest(exportExecutor, requestContext);
                limitingService.limiting(limitingRequest);
                async = executor.enableAsync(requestContext);
            }

            // 发布下载开始-事件
            publishDownloadStartEvent(executor, requestContext, async);

            // 是否开启异步
            if (!async) {
                try {
                    Object syncResult = executeSync(invocation);
                    publishDownloadEndEvent(executor, requestContext, false, null, syncResult);
                    return syncResult;
                } catch (Throwable throwable) {
                    log.error("FileExportInterceptor,executeSync fail!requestContext:{}", requestContext, throwable);
                    publishDownloadEndEvent(executor, requestContext, false, throwable, null);
                    throw new EasyFileException(SYNC_DOWNLOAD_EXECUTE_ERROR);
                }
            } else {
                try {
                    Pair<Boolean, Long> resultPair = executeAsync(invocation, executor, requestContext, exportExecutor);
                    publishDownloadEndEvent(executor, requestContext, true, null, resultPair);
                    return resultPair;
                } catch (Throwable ex) {
                    log.error("FileExportInterceptor,executeAsync fail!requestContext:{}", requestContext, ex);
                    publishDownloadEndEvent(executor, requestContext, true, ex, null);
                    throw ex;
                }
            }
        } finally {
            PageTotalContext.clear();
        }

    }


    /**
     * 执行同步导出function
     *
     * @param invocation pjp
     * @return 执行结果
     */
    private Object executeSync(MethodInvocation invocation) throws Throwable {
        if (invocation.getThis() instanceof BaseWrapperSyncResponseHeader) {
            BaseWrapperSyncResponseHeader header = (BaseWrapperSyncResponseHeader) invocation.getThis();

            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) attributes;
                HttpServletResponse response = requestAttributes.getResponse();
                if (Objects.nonNull(response)) {
                    header.setSyncResponseHeader(response);
                }
            }
        }
        return invocation.proceed();
    }


    /**
     * 执行异步导出
     *
     * @param pjp pjp
     * @param executor 执行器
     * @param requestContext 请求上线文
     * @param exportExecutor 文件导出执行器
     * @return 执行结果
     */
    private Pair<Boolean, Long> executeAsync(MethodInvocation pjp, BaseDownloadExecutor executor,
        DownloaderRequestContext requestContext, FileExportExecutor exportExecutor) {
        // 先执行同步注册
        RegisterDownloadRequest downloadRequest = buildDownloadRequest(exportExecutor, requestContext);
        TransactionTemplate template = context.getBean(TransactionTemplate.class);
        Long downloadRegisterId = template.execute(action -> {
            Long registerId = downloadStorageService.register(downloadRequest);
            handler.execute(executor, requestContext, registerId);
            return registerId;
        });

        if (EXPORT_RESULT_METHOD_NAME.equals(pjp.getMethod().getName())) {
            return Pair.of(Boolean.TRUE, downloadRegisterId);
        } else {
            return null;
        }
    }

    /**
     * 发布下载结束Event
     *
     * @param executor 执行器
     * @param requestContext 请求上下文
     * @param async 是否异步
     */
    private void publishDownloadEndEvent(BaseDownloadExecutor executor, DownloaderRequestContext requestContext,
        boolean async, Throwable exception, Object result) {

        DownloadEndEvent endEvent = new DownloadEndEvent(requestContext, executor, async, exception, result);
        // 执行发布事件到监听器
        Map<String, DownloadEndListener> startListenerMap = context.getBeansOfType(DownloadEndListener.class);
        for (DownloadEndListener listener : startListenerMap.values()) {
            listener.listen(endEvent);
        }
    }


    /**
     * 发布下载开始事件
     *
     * @param executor executors
     * @param requestContext 请求上下文
     * @param async 是否异步
     */
    private void publishDownloadStartEvent(BaseDownloadExecutor executor, DownloaderRequestContext requestContext,
        boolean async) {
        DownloadStartEvent startEvent = new DownloadStartEvent(requestContext, executor, async);
        // 执行发布事件到监听器
        Map<String, DownloadStartListener> startListenerMap = context.getBeansOfType(DownloadStartListener.class);
        for (DownloadStartListener listener : startListenerMap.values()) {
            listener.listen(startEvent);
        }
    }

    private ExportLimitingRequest buildLimitingRequest(FileExportExecutor executor,
        DownloaderRequestContext requestContext) {
        ExportLimitingRequest limitingRequest = new ExportLimitingRequest();
        limitingRequest.setAppId(downloadProperties.getAppId());
        limitingRequest.setIpAddr(IpUtil.getIp());
        limitingRequest.setDownloadCode(executor.value());
        limitingRequest.setFileSuffix(requestContext.getFileSuffix());
        limitingRequest.setOtherMap(requestContext.getOtherMap());
        return limitingRequest;
    }

    private RegisterDownloadRequest buildDownloadRequest(FileExportExecutor executor,
        DownloaderRequestContext requestContext) {
        RegisterDownloadRequest registerRequest = new RegisterDownloadRequest();
        registerRequest.setAppId(downloadProperties.getAppId());
        registerRequest.setDownloadCode(executor.value());
        registerRequest.setEnableNotify(executor.enableNotify());
        registerRequest.setNotifier(requestContext.getNotifier());
        registerRequest.setExportRemark(requestContext.getExportRemark());
        registerRequest.setFileSuffix(requestContext.getFileSuffix());
        registerRequest.setOtherMap(requestContext.getOtherMap());
        registerRequest.setMaxServerRetry(executor.maxServerRetry());
        return registerRequest;
    }
}
