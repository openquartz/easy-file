package com.openquartz.easyfile.starter.aop;

import static com.openquartz.easyfile.core.exception.ImportErrorCode.FILE_IMPORT_EXECUTOR_MUST_SUPPORT_ANNOTATION;
import static com.openquartz.easyfile.core.exception.ImportErrorCode.SYNC_IMPORT_EXECUTE_ERROR;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.i18n.LocaleContext;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.core.executor.BaseAsyncImportHandler;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.core.executor.support.FileImportTriggerContext;
import com.openquartz.easyfile.core.intercept.listener.ImportEndEvent;
import com.openquartz.easyfile.core.intercept.listener.ImportEndListener;
import com.openquartz.easyfile.core.intercept.listener.ImportStartEvent;
import com.openquartz.easyfile.core.intercept.listener.ImportStartListener;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 文件导入方法拦截器
 *
 * @author svnee
 */
@Slf4j
public class FileImportInterceptor implements MethodInterceptor {

    private final EasyFileImportProperties importProperties;
    private final EasyFileDownloadProperties downloadProperties;
    private final LimitingService limitingService;
    private final ImportStorageService importStorageService;
    private final BaseAsyncImportHandler handler;
    private final ApplicationContext context;

    private static final String IMPORT_RESULT_METHOD_NAME = "importResult";

    public FileImportInterceptor(EasyFileImportProperties importProperties,
        EasyFileDownloadProperties downloadProperties,
        LimitingService limitingService,
        ImportStorageService importStorageService,
        BaseAsyncImportHandler handler,
        ApplicationContext context) {
        this.importProperties = importProperties;
        this.downloadProperties = downloadProperties;
        this.limitingService = limitingService;
        this.importStorageService = importStorageService;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        // direct trigger if async trigger
        if (FileImportTriggerContext.isAsyncTrigger()) {
            return invocation.proceed();
        }

        // import disabled → proceed default path
        if (!importProperties.isEnabled()) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArguments();
        BaseImportExecutor<?> executor = (BaseImportExecutor<?>) invocation.getThis();
        ImportRequestContext requestContext = (ImportRequestContext) args[0];
        Class<?> clazz = SpringContextUtil.getRealClass(executor);
        FileImportExecutor importExecutor = clazz.getDeclaredAnnotation(FileImportExecutor.class);
        Asserts.notNull(importExecutor, FILE_IMPORT_EXECUTOR_MUST_SUPPORT_ANNOTATION);

        ImportContext enableAsyncContext = buildImportContext(requestContext, importExecutor, null);
        boolean async = executor.enableAsync(enableAsyncContext);

        String importTraceId = UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
        publishImportStartEvent(executor, requestContext, async, importTraceId);

        if (!async) {
            try {
                Pair<Boolean, Long> syncResult = executeSync(executor, requestContext, importExecutor);
                publishImportEndEvent(executor, requestContext, false, null, syncResult, importTraceId);
                return syncResult;
            } catch (Throwable throwable) {
                log.error("FileImportInterceptor,executeSync fail!requestContext:{}", requestContext, throwable);
                publishImportEndEvent(executor, requestContext, false, throwable, null, importTraceId);
                throw new EasyFileException(SYNC_IMPORT_EXECUTE_ERROR);
            }
        }

        try {
            Pair<Boolean, Long> resultPair = executeAsync(invocation, executor, requestContext, importExecutor);
            publishImportEndEvent(executor, requestContext, true, null, resultPair, importTraceId);
            return resultPair;
        } catch (Throwable ex) {
            log.error("FileImportInterceptor,executeAsync fail!requestContext:{}", requestContext, ex);
            publishImportEndEvent(executor, requestContext, true, ex, null, importTraceId);
            throw ex;
        }
    }

    private Pair<Boolean, Long> executeSync(BaseImportExecutor<?> executor, ImportRequestContext requestContext,
        FileImportExecutor importExecutor) {
        Long registerId = requestContext.getRegisterId();
        if (importProperties.isSyncPersist()) {
            registerId = importStorageService.register(buildRegisterRequest(importExecutor, requestContext, false));
            requestContext.setRegisterId(registerId);
        }
        ImportContext importContext = buildImportContext(requestContext, importExecutor, registerId);
        ImportProcessResult processResult = executor.process(importContext);
        log.debug("FileImportInterceptor sync process done, registerId:{}, result:{}", registerId, processResult);
        return Pair.of(Boolean.FALSE, registerId);
    }

    private Pair<Boolean, Long> executeAsync(MethodInvocation invocation, BaseImportExecutor<?> executor,
        ImportRequestContext requestContext, FileImportExecutor importExecutor) {
        ImportLimitingRequest limitingRequest = buildLimitingRequest(importExecutor, requestContext);
        limitingService.limiting(limitingRequest);

        ImportRegisterRequest registerRequest = buildRegisterRequest(importExecutor, requestContext, true);
        TransactionTemplate template = context.getBean(TransactionTemplate.class);
        Long importRegisterId = template.execute(action -> {
            Long registerId = importStorageService.register(registerRequest);
            handler.execute(executor, requestContext, registerId);
            return registerId;
        });

        if (IMPORT_RESULT_METHOD_NAME.equals(invocation.getMethod().getName())) {
            return Pair.of(Boolean.TRUE, importRegisterId);
        }
        return null;
    }

    private void publishImportEndEvent(BaseImportExecutor<?> executor, ImportRequestContext requestContext,
        boolean async, Throwable exception, Object result, String importTraceId) {

        ImportEndEvent endEvent = new ImportEndEvent(requestContext, executor, async, exception, result,
            importTraceId);

        Map<String, ImportEndListener> endListenerMap = context.getBeansOfType(ImportEndListener.class);
        for (ImportEndListener listener : endListenerMap.values()) {
            listener.listen(endEvent);
        }
    }

    private void publishImportStartEvent(BaseImportExecutor<?> executor, ImportRequestContext requestContext,
        boolean async, String importTraceId) {

        ImportStartEvent startEvent = new ImportStartEvent(requestContext, executor, async, importTraceId);

        Map<String, ImportStartListener> startListenerMap = context.getBeansOfType(ImportStartListener.class);
        for (ImportStartListener listener : startListenerMap.values()) {
            listener.listen(startEvent);
        }
    }

    private ImportLimitingRequest buildLimitingRequest(FileImportExecutor executor,
        ImportRequestContext requestContext) {
        ImportLimitingRequest limitingRequest = new ImportLimitingRequest();
        limitingRequest.setAppId(downloadProperties.getAppId());
        limitingRequest.setImportCode(executor.value());
        limitingRequest.setOperator(requestContext.getOperator());
        return limitingRequest;
    }

    private ImportRegisterRequest buildRegisterRequest(FileImportExecutor executor,
        ImportRequestContext requestContext, boolean enableAsync) {
        String locale = StringUtils.isNotBlank(requestContext.getLocale())
            ? requestContext.getLocale()
            : LocaleContext.currentLocaleLanguage();
        return ImportRegisterRequest.builder()
            .appId(downloadProperties.getAppId())
            .importCode(executor.value())
            .ossUrl(requestContext.getSourceFileUrl())
            .fileName(requestContext.getFileName())
            .fileType(requestContext.getFileType())
            .businessUniqueKey(requestContext.getBusinessUniqueKey())
            .executeParam(requestContext.getExecuteParam())
            .createBy(requestContext.getOperator())
            .enableAsync(enableAsync)
            .supportResumable(true)
            .pageSize(null)
            .maxRetry(executor.maxServerRetry())
            .notifyEmail(requestContext.getNotifyEmail())
            .locale(locale)
            .enableNotify(executor.enableNotify())
            .build();
    }

    private ImportContext buildImportContext(ImportRequestContext requestContext, FileImportExecutor importExecutor,
        Long registerId) {
        return ImportContext.builder()
            .registerId(registerId != null ? registerId : requestContext.getRegisterId())
            .appId(downloadProperties.getAppId())
            .importCode(importExecutor.value())
            .ossUrl(requestContext.getSourceFileUrl())
            .fileName(requestContext.getFileName())
            .fileType(requestContext.getFileType())
            .businessUniqueKey(requestContext.getBusinessUniqueKey())
            .executeParam(requestContext.getExecuteParam())
            .enableNotify(importExecutor.enableNotify())
            .maxRetry(importExecutor.maxServerRetry())
            .createBy(requestContext.getOperator())
            .build();
    }
}
