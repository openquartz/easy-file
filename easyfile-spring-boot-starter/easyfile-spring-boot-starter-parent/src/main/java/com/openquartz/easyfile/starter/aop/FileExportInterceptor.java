package com.openquartz.easyfile.starter.aop;

import static com.openquartz.easyfile.core.exception.DownloadErrorCode.FILE_GENERATOR_MUST_SUPPORT_ANNOTATION;
import static com.openquartz.easyfile.core.exception.DownloadErrorCode.SYNC_DOWNLOAD_EXECUTE_ERROR;

import com.openquartz.easyfile.common.i18n.LocaleContextHolder;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.executor.support.FileExportTriggerContext;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.util.page.PageTotalContext;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.request.ExportLimitingRequest;
import com.openquartz.easyfile.common.request.RegisterDownloadRequest;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;
import com.openquartz.easyfile.core.intercept.listener.DownloadEndEvent;
import com.openquartz.easyfile.core.intercept.listener.DownloadEndListener;
import com.openquartz.easyfile.core.intercept.listener.DownloadStartEvent;
import com.openquartz.easyfile.core.intercept.listener.DownloadStartListener;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.download.LimitingService;

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
    public Object invoke(MethodInvocation invocation) throws Throwable {

        // direct trigger if async trigger
        if (FileExportTriggerContext.isAsyncTrigger()){
            return invocation.proceed();
        }

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
            String downloadTraceId = UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
            publishDownloadStartEvent(executor, requestContext, async, downloadTraceId);

            // 是否开启异步
            if (!async) {
                try {
                    Object syncResult = executeSync(invocation);
                    publishDownloadEndEvent(executor, requestContext, false, null, syncResult, downloadTraceId);
                    return syncResult;
                } catch (Throwable throwable) {
                    log.error("FileExportInterceptor,executeSync fail!requestContext:{}", requestContext, throwable);
                    publishDownloadEndEvent(executor, requestContext, false, throwable, null, downloadTraceId);
                    throw new EasyFileException(SYNC_DOWNLOAD_EXECUTE_ERROR);
                }
            } else {
                try {
                    Pair<Boolean, Long> resultPair = executeAsync(invocation, executor, requestContext, exportExecutor);
                    publishDownloadEndEvent(executor, requestContext, true, null, resultPair, downloadTraceId);
                    return resultPair;
                } catch (Throwable ex) {
                    log.error("FileExportInterceptor,executeAsync fail!requestContext:{}", requestContext, ex);
                    publishDownloadEndEvent(executor, requestContext, true, ex, null, downloadTraceId);
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
        boolean async, Throwable exception, Object result, String downloadTraceId) {

        DownloadEndEvent endEvent = new DownloadEndEvent(requestContext, executor, async, exception, result,
            downloadTraceId);

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
        boolean async,
        String downloadTraceId) {

        // 构建开始事件
        DownloadStartEvent startEvent = new DownloadStartEvent(requestContext, executor, async, downloadTraceId);

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
        registerRequest.setLocale(LocaleContextHolder.currentLocaleLanguage());
        return registerRequest;
    }
}
