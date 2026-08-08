package com.openquartz.easyfile.core.executor.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.core.annotations.FileImportExecutor;
import com.openquartz.easyfile.core.context.ImportContext;
import com.openquartz.easyfile.core.context.ImportDataContext;
import com.openquartz.easyfile.core.context.ImportProcessContext;
import com.openquartz.easyfile.core.entity.ImportProcessResult;
import com.openquartz.easyfile.core.entity.ProcessResult;
import com.openquartz.easyfile.core.entity.ValidationResult;
import com.openquartz.easyfile.core.executor.BaseImportExecutor;
import org.junit.Test;

/**
 * Registry null-safety for unknown import codes.
 */
public class FileImportExecutorSupportTest {

    @Test
    public void getShouldThrowWhenCodeMissing() {
        try {
            FileImportExecutorSupport.get("not-exist-import-" + System.nanoTime());
            fail("expected EasyFileException");
        } catch (EasyFileException ex) {
            assertNotNull(ex);
        } catch (NullPointerException npe) {
            fail("must not throw NPE for missing executor");
        }
    }

    @Test
    public void registerAndGetShouldReturnExecutor() {
        String code = "import-ut-" + System.nanoTime();
        DummyImportExecutor executor = new DummyImportExecutor();
        FileImportExecutor annotation = DummyImportExecutor.class.getAnnotation(FileImportExecutor.class);
        FileImportExecutorSupport.register(code, annotation, executor);
        assertTrue(FileImportExecutorSupport.contains(code));
        assertEquals(executor, FileImportExecutorSupport.get(code));
    }

    @FileImportExecutor(value = "dummy-import-ut", desc = "ut")
    private static class DummyImportExecutor implements BaseImportExecutor<Object> {
        @Override
        public ImportProcessResult process(ImportContext context) {
            return ImportProcessResult.builder().success(true).build();
        }

        @Override
        public ValidationResult validate(ImportDataContext<Object> context) {
            return ValidationResult.success();
        }

        @Override
        public ProcessResult processBusiness(java.util.List<Object> data, ImportProcessContext context) {
            return ProcessResult.success(data.size());
        }

        @Override
        public int getPageSize() {
            return 100;
        }
    }
}
