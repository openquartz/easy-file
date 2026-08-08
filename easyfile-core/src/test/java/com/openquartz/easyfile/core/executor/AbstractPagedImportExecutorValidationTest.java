package com.openquartz.easyfile.core.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.lock.Consumer;
import com.openquartz.easyfile.common.lock.LockSupport;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportDataContext;
import com.openquartz.easyfile.core.context.ImportProcessContext;
import com.openquartz.easyfile.core.entity.ErrorRecord;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.core.entity.ProcessResult;
import com.openquartz.easyfile.core.entity.ValidationResult;
import com.openquartz.easyfile.core.parser.ImportParser;
import com.openquartz.easyfile.core.service.IdempotentImportManager;
import com.openquartz.easyfile.core.ImportResultFileGenerator;
import com.openquartz.easyfile.storage.file.DownloadService;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Validation failures must not invoke business processing or mark overall success.
 */
public class AbstractPagedImportExecutorValidationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private IdempotentImportManager idempotentManager;
    private LockSupport lockSupport;
    private ImportStorageService importStorageService;
    private DownloadService downloadService;
    private ImportResultFileGenerator importResultFileGenerator;
    private TestExecutor executor;
    private final AtomicBoolean businessCalled = new AtomicBoolean(false);

    @Before
    public void setUp() {
        idempotentManager = mock(IdempotentImportManager.class);
        lockSupport = mock(LockSupport.class);
        importStorageService = mock(ImportStorageService.class);
        downloadService = mock(DownloadService.class);
        importResultFileGenerator = mock(ImportResultFileGenerator.class);
        executor = new TestExecutor(businessCalled);

        ReflectionTestUtils.setField(executor, "idempotentManager", idempotentManager);
        ReflectionTestUtils.setField(executor, "lockSupport", lockSupport);
        ReflectionTestUtils.setField(executor, "importStorageService", importStorageService);
        ReflectionTestUtils.setField(executor, "downloadService", downloadService);
        ReflectionTestUtils.setField(executor, "importResultFileGenerator", importResultFileGenerator);

        when(idempotentManager.isExecuted(anyString())).thenReturn(false);
        doNothing().when(idempotentManager).markAsExecuted(anyString(), any(ImportResult.class));
        doNothing().when(importStorageService).markExecuting(anyLong());
        doNothing().when(importStorageService).refreshProgress(anyLong(), nullable(Integer.class), anyLong());
        doNothing().when(importStorageService).updateTotalCount(anyLong(), anyLong());
        doNothing().when(importStorageService).updateFileHash(anyLong(), anyString());
        doNothing().when(importStorageService).complete(anyLong(), any(ImportStatusEnum.class), anyLong(),
            anyLong(), anyLong(), anyLong(), nullable(String.class));
        doNothing().when(importStorageService).updateResultFileUrl(anyLong(), anyString());
        doReturn("http://result").when(importResultFileGenerator)
            .generate(anyString(), anyLong(), anyList(), anyLong(), anyLong(), anyLong());
        doAnswer(invocation -> {
            File file = invocation.getArgument(1);
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.write(file.toPath(), new byte[]{1, 2, 3});
            return null;
        }).when(downloadService).download(anyString(), any(File.class));
        when(lockSupport.consumeIfTryLock(
            org.mockito.ArgumentMatchers.<Pair<String, com.openquartz.easyfile.common.lock.LockBizType>>any(),
            org.mockito.ArgumentMatchers.any(Consumer.class)
        )).thenAnswer(invocation -> {
            Consumer consumer = invocation.getArgument(1);
            consumer.consume();
            return true;
        });
    }

    @Test
    public void validationFailureShouldSkipBusinessAndMarkFail() throws Exception {
        File tempDir = temporaryFolder.newFolder("import-fail");
        ImportParser<String> parser = mock(ImportParser.class);
        when(parser.getTotalCount()).thenReturn(1L);
        when(parser.readPage(1, 1000)).thenReturn(new java.util.ArrayList<>(Collections.singletonList("bad-row")));
        doNothing().when(parser).close();
        executor.setParser(parser);
        executor.setTempOverride(tempDir.getAbsolutePath());

        ImportContext context = ImportContext.builder()
            .registerId(10L)
            .appId("app")
            .importCode("code")
            .ossUrl("https://example.com/a.xlsx")
            .fileName("import.xlsx")
            .fileType("xlsx")
            .businessUniqueKey("biz-1")
            .build();

        ImportProcessResult result = executor.process(context);

        assertFalse(result.isSuccess());
        assertEquals(1, result.getFailCount());
        assertFalse(businessCalled.get());
        verify(idempotentManager, never()).markAsExecuted(anyString(), any(ImportResult.class));
    }

    @Test
    public void successPathShouldMarkIdempotent() throws Exception {
        File tempDir = temporaryFolder.newFolder("import-ok");
        ImportParser<String> parser = mock(ImportParser.class);
        when(parser.getTotalCount()).thenReturn(1L);
        when(parser.readPage(1, 1000)).thenReturn(new java.util.ArrayList<>(Collections.singletonList("ok-row")));
        doNothing().when(parser).close();
        executor.setParser(parser);
        executor.setTempOverride(tempDir.getAbsolutePath());
        executor.setValidationSuccess(true);

        ImportContext context = ImportContext.builder()
            .registerId(11L)
            .appId("app")
            .importCode("code")
            .ossUrl("https://example.com/b.xlsx")
            .fileName("ok.xlsx")
            .fileType("xlsx")
            .businessUniqueKey("biz-2")
            .build();

        businessCalled.set(false);
        ImportProcessResult result = executor.process(context);

        assertTrue("expected success, errorMsg=" + result.getErrorMsg()
                + ", failCount=" + result.getFailCount()
                + ", successCount=" + result.getSuccessCount()
                + ", businessCalled=" + businessCalled.get(),
            result.isSuccess());
        assertEquals(1, result.getSuccessCount());
        assertTrue(businessCalled.get());
        verify(idempotentManager).markAsExecuted(anyString(), any(ImportResult.class));
    }

    private static final class TestExecutor extends AbstractPagedImportExecutor<String> {

        private final AtomicBoolean businessCalled;
        private ImportParser<String> parser;
        private String tempOverride;
        private boolean validationSuccess;

        private TestExecutor(AtomicBoolean businessCalled) {
            this.businessCalled = businessCalled;
            this.validationSuccess = false;
        }

        void setParser(ImportParser<String> parser) {
            this.parser = parser;
        }

        void setTempOverride(String tempOverride) {
            this.tempOverride = tempOverride;
        }

        void setValidationSuccess(boolean validationSuccess) {
            this.validationSuccess = validationSuccess;
        }

        @Override
        protected Class<String> getTargetClass() {
            return String.class;
        }

        @Override
        protected String getTempDirectory() {
            return tempOverride != null ? tempOverride : super.getTempDirectory();
        }

        @Override
        protected ImportParser<String> createParser(File file, String fileType) {
            return parser;
        }

        @Override
        public ValidationResult validate(ImportDataContext<String> dataContext) {
            if (validationSuccess) {
                return ValidationResult.success();
            }
            return ValidationResult.failure(Collections.singletonList(ErrorRecord.of(1, "invalid")));
        }

        @Override
        public ProcessResult processBusiness(List<String> data, ImportProcessContext processContext) {
            businessCalled.set(true);
            return ProcessResult.success(data.size());
        }
    }
}
