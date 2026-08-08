package com.openquartz.easyfile.core.executor.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import org.junit.Test;

/**
 * Registry null-safety for unknown download codes.
 */
public class FileExportExecutorSupportTest {

    @Test
    public void getShouldThrowWhenCodeMissing() {
        try {
            FileExportExecutorSupport.get("not-exist-code-" + System.nanoTime());
            fail("expected EasyFileException");
        } catch (EasyFileException ex) {
            assertTrue(ex.getMessage() == null
                || ex.getMessage().contains("not")
                || ex.getErrorCode() != null);
        } catch (NullPointerException npe) {
            fail("must not throw NPE for missing executor");
        }
    }

    @Test
    public void registerAndGetShouldReturnExecutor() {
        String code = "export-ut-" + System.nanoTime();
        DummyExecutor executor = new DummyExecutor();
        FileExportExecutor annotation = DummyExecutor.class.getAnnotation(FileExportExecutor.class);
        FileExportExecutorSupport.register(code, annotation, executor);
        assertTrue(FileExportExecutorSupport.contains(code));
        assertNotNull(FileExportExecutorSupport.get(code));
        assertEquals(executor, FileExportExecutorSupport.get(code));
    }

    @FileExportExecutor(value = "dummy-export-ut", desc = "ut")
    private static class DummyExecutor implements BaseDownloadExecutor {
        @Override
        public boolean enableAsync(BaseDownloaderRequestContext context) {
            return false;
        }

        @Override
        public void export(DownloaderRequestContext context) {
            // no-op
        }
    }
}
