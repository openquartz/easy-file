package com.openquartz.easyfile.starter.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.request.ImportLimitingRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportDataContext;
import com.openquartz.easyfile.core.context.ImportProcessContext;
import com.openquartz.easyfile.core.context.ImportRequestContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.core.entity.ProcessResult;
import com.openquartz.easyfile.core.entity.ValidationResult;
import com.openquartz.easyfile.core.executor.BaseAsyncImportHandler;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadProperties;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileImportProperties;
import com.openquartz.easyfile.storage.download.LimitingService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * FileImportInterceptor unit tests.
 */
public class FileImportInterceptorTest {

    private EasyFileImportProperties importProperties;
    private EasyFileDownloadProperties downloadProperties;
    private LimitingService limitingService;
    private ImportStorageService importStorageService;
    private BaseAsyncImportHandler handler;
    private ApplicationContext applicationContext;
    private TransactionTemplate transactionTemplate;
    private FileImportInterceptor interceptor;

    @Before
    public void setUp() {
        importProperties = new EasyFileImportProperties();
        importProperties.setEnabled(true);
        downloadProperties = new EasyFileDownloadProperties();
        downloadProperties.setAppId("app-1");
        limitingService = mock(LimitingService.class);
        importStorageService = mock(ImportStorageService.class);
        handler = mock(BaseAsyncImportHandler.class);
        applicationContext = mock(ApplicationContext.class);
        transactionTemplate = mock(TransactionTemplate.class);
        when(applicationContext.getBean(TransactionTemplate.class)).thenReturn(transactionTemplate);
        when(applicationContext.getBeansOfType(any())).thenReturn(Collections.emptyMap());

        interceptor = new FileImportInterceptor(
            importProperties,
            downloadProperties,
            limitingService,
            importStorageService,
            handler,
            applicationContext
        );
    }

    @Test
    public void asyncPath_registersAndExecutesHandler() throws Throwable {
        DemoImportExecutor executor = new DemoImportExecutor();
        ImportRequestContext requestContext = new ImportRequestContext();
        requestContext.setSourceFileUrl("http://oss/file.xlsx");
        requestContext.setFileName("file.xlsx");
        requestContext.setFileType("xlsx");
        requestContext.setOperator("alice");

        when(importStorageService.register(any(ImportRegisterRequest.class))).thenReturn(9L);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        Method method = BaseImportExecutor.class.getMethod("importResult", ImportRequestContext.class);
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getThis()).thenReturn(executor);
        when(invocation.getArguments()).thenReturn(new Object[]{requestContext});
        when(invocation.getMethod()).thenReturn(method);

        Object result = interceptor.invoke(invocation);

        assertTrue(result instanceof Pair);
        @SuppressWarnings("unchecked")
        Pair<Boolean, Long> pair = (Pair<Boolean, Long>) result;
        assertEquals(Boolean.TRUE, pair.getKey());
        assertEquals(Long.valueOf(9L), pair.getValue());

        verify(limitingService).limiting(any(ImportLimitingRequest.class));
        verify(importStorageService).register(any(ImportRegisterRequest.class));
        verify(handler).execute(executor, requestContext, 9L);
    }

    @FileImportExecutor(value = "demoImport", desc = "demo")
    private static class DemoImportExecutor implements BaseImportExecutor<Object> {

        @Override
        public boolean enableAsync(ImportContext context) {
            return true;
        }

        @Override
        public ImportProcessResult process(ImportContext context) {
            return ImportProcessResult.builder().success(true).build();
        }

        @Override
        public ValidationResult validate(ImportDataContext<Object> dataContext) {
            return ValidationResult.builder().success(true).build();
        }

        @Override
        public ProcessResult processBusiness(List<Object> data, ImportProcessContext processContext) {
            return ProcessResult.builder().success(true).build();
        }
    }
}
