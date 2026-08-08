package com.openquartz.easyfile.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for safe temp file path resolution (path traversal).
 */
public class ImportTempFilesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldResolveSafeFileNameUnderTempDir() throws Exception {
        File tempDir = temporaryFolder.newFolder("import");
        File target = ImportTempFiles.resolveTempFile(tempDir.getAbsolutePath(), "data.xlsx", 100L, "xlsx");
        assertTrue(target.getCanonicalPath().startsWith(tempDir.getCanonicalPath()));
        assertEquals("data.xlsx", target.getName());
    }

    @Test
    public void shouldRejectPathTraversalFileName() throws Exception {
        File tempDir = temporaryFolder.newFolder("import");
        try {
            ImportTempFiles.resolveTempFile(tempDir.getAbsolutePath(), "../../evil.xlsx", 100L, "xlsx");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("path")
                || expected.getMessage().contains("非法")
                || expected.getMessage().contains("traversal"));
        }
    }

    @Test
    public void shouldUseGeneratedNameWhenFileNameMissing() throws Exception {
        File tempDir = temporaryFolder.newFolder("import");
        File target = ImportTempFiles.resolveTempFile(tempDir.getAbsolutePath(), null, 88L, "xlsx");
        assertEquals("import_88.xlsx", target.getName());
        assertTrue(target.getCanonicalPath().startsWith(tempDir.getCanonicalPath() + File.separator)
            || target.getCanonicalPath().startsWith(tempDir.getCanonicalPath()));
        Files.createDirectories(target.getParentFile().toPath());
    }

    @Test
    public void shouldRejectFileSeparatorInFileName() throws Exception {
        File tempDir = temporaryFolder.newFolder("import");
        try {
            ImportTempFiles.resolveTempFile(tempDir.getAbsolutePath(), "sub/evil.xlsx", 1L, "xlsx");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }
}
